package com.sovnem.bisheng

import com.squareup.javapoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic
import javax.tools.StandardLocation

class AdapterProcessor : AbstractProcessor() {

    private lateinit var mFiler: Filer
    private lateinit var elementUtil: Elements
    private lateinit var messager: Messager
    
    // 用于生成唯一的类名（支持多模块）
    private var moduleIdentifier: String = ""

    private var hasProceed = false
    
    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
        mFiler = p0.filer
        elementUtil = p0.elementUtils
        messager = p0.messager
        
        // 获取模块标识符（基于构建路径）
        moduleIdentifier = generateModuleIdentifier(p0)
        
        log("BiSheng注解处理器初始化完成")
        log("模块标识符: $moduleIdentifier")
    }
    
    /**
     * 生成模块唯一标识符
     * 基于模块的输出路径生成一个唯一的标识符
     */
    private fun generateModuleIdentifier(env: ProcessingEnvironment): String {
        return try {
            // 尝试从编译参数中获取模块名
            val moduleName = env.options["kapt.kotlin.generated"] 
                ?: env.options["org.gradle.appname"]
            
            if (moduleName != null) {
                // 清理模块名，只保留字母数字和下划线
                val cleanName = moduleName
                    .substringAfterLast(File.separator)
                    .substringAfterLast("/")
                    .replace(Regex("[^a-zA-Z0-9_]"), "_")
                    .take(20)
                if (cleanName.isNotEmpty()) {
                    return cleanName
                }
            }
            
            // 如果无法获取模块名，使用时间戳的 hash
            val timestamp = System.currentTimeMillis()
            "Module_${(timestamp % 100000).toString().padStart(5, '0')}"
        } catch (e: Exception) {
            log("无法生成模块标识符，使用默认值: ${e.message}")
            ""
        }
    }
    
    private fun log(message: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, "[BiSheng] $message")
    }
    
    private fun error(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, "[BiSheng Error] $message")
    }
    
    private fun warning(message: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, "[BiSheng Warning] $message")
    }
    
    /**
     * 生成 ServiceLoader 配置文件
     * 用于自动发现多模块中的映射实现
     */
    private fun generateServiceLoaderConfig(className: String) {
        try {
            val serviceFile = mFiler.createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                "META-INF/services/${Constants.PACKAGE}.IAdapterMap"
            )
            
            serviceFile.openWriter().use { writer ->
                writer.write("${Constants.PACKAGE}.$className")
            }
            
            log("成功生成 ServiceLoader 配置文件")
        } catch (e: Exception) {
            // 如果文件已存在或生成失败，只记录警告，不影响主流程
            warning("生成 ServiceLoader 配置文件失败: ${e.message}")
        }
    }

    override fun process(
        set: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        try {
            if (hasProceed) return false

            val holderClass = ArrayList<String>()

            val dataClassStrToVhRef = HashMap<String, String>()

            val vhResMap = HashMap<String, Int>()

            val dataFullToHolderSimple = HashMap<String, String>()
            val vhSimpleToFull = HashMap<String, String>()
            
            // 处理VHRef注解
            val vhRefElements = roundEnvironment.getElementsAnnotatedWith(VHRef::class.java)
            log("找到 ${vhRefElements.size} 个@VHRef注解")
            
            vhRefElements.forEach {
                val an = it.getAnnotation(VHRef::class.java)
                if (!an.lazyLoad) {
                    val anno = an.toString()
                    val dataClassName =
                        elementUtil.getPackageOf(it).toString() + "." + it.simpleName
                    holderClass.add(dataClassName)
                    val strs = anno.split(".")
                    val holderName = strs[strs.size - 1].replace(")", "")
                    dataFullToHolderSimple[dataClassName] = holderName
                    log("注册数据类: $dataClassName -> ViewHolder: $holderName")
                }
            }
            
            // 处理VHLayoutId注解
            val vhLayoutIdElements = roundEnvironment.getElementsAnnotatedWith(VHLayoutId::class.java)
            log("找到 ${vhLayoutIdElements.size} 个@VHLayoutId注解")
            
            vhLayoutIdElements.forEach {
                val ann = it.getAnnotation(VHLayoutId::class.java)
                if (!ann.lazyLoad) {
                    val vhClassName = elementUtil.getPackageOf(it).toString() + "." + it.simpleName
                    vhSimpleToFull[it.simpleName.toString()] = vhClassName
                    val layoutId = ann.layoutId
                    // 跳过无效的 layoutId（KAPT stub 生成时可能为 0）
                    if (layoutId != 0) {
                        vhResMap[vhClassName] = layoutId
                        log("注册ViewHolder: $vhClassName, layoutId: $layoutId")
                    } else {
                        warning("ViewHolder $vhClassName 的 layoutId 为 0，将在运行时通过反射获取")
                    }
                }
            }
            
            // 建立映射关系
            holderClass.forEach {
                val vhName = dataFullToHolderSimple[it]
                val vhFullName = vhSimpleToFull[vhName]
                if (vhFullName == null) {
                    error("无法找到ViewHolder: $vhName 的完整类名，请确保ViewHolder类也使用了@VHLayoutId注解")
                    return@forEach
                }
                dataClassStrToVhRef[it] = vhFullName
            }
            // 使用 ClassName 代替 Class.forName，提高类型安全性
            val arrayMapType = ClassName.get("android.util", "ArrayMap")
            val sparseArrayType = ClassName.get("android.util", "SparseArray")
            val classType = ClassName.get("java.lang", "Class")
            val integerType = ClassName.get("java.lang", "Integer")
            
            // 泛型类型：ArrayMap<Class<?>, Integer>
            val arrayMapClassIntType = ParameterizedTypeName.get(
                arrayMapType,
                ParameterizedTypeName.get(classType, WildcardTypeName.subtypeOf(TypeName.OBJECT)),
                integerType
            )
            
            // 泛型类型：SparseArray<Class<?>>
            val sparseArrayClassType = ParameterizedTypeName.get(
                sparseArrayType,
                ParameterizedTypeName.get(classType, WildcardTypeName.subtypeOf(TypeName.OBJECT))
            )
            
            val constructor =
                MethodSpec.constructorBuilder()
                    .apply {
                        holderClass.forEach {
                            val vhFullName = dataClassStrToVhRef[it]
                            val resId = vhResMap[vhFullName]
                            addStatement(
                                "dataToType.put(\$T.class, \$T.class.hashCode())",
                                ClassName.bestGuess(it),
                                ClassName.bestGuess(it)
                            )
                            addStatement(
                                "typeToViewHolder.put(\$T.class.hashCode(), \$T.class)",
                                ClassName.bestGuess(it),
                                ClassName.bestGuess(vhFullName!!)
                            )
                            if (resId != null && resId != 0) {
                                addStatement(
                                    "viewHolderToLayoutRes.put(\$T.class, \$L)",
                                    ClassName.bestGuess(vhFullName),
                                    resId
                                )
                            }
                        }
                    }.addModifiers(Modifier.PUBLIC).build()

            val getdataToType =
                MethodSpec.methodBuilder("getDataToType")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(arrayMapClassIntType)
                    .addStatement("return dataToType")
                    .build()

            val dataToTypeField = FieldSpec.builder(
                arrayMapClassIntType,
                "dataToType",
                Modifier.PROTECTED
            ).initializer("new \$T<>()", arrayMapType).build()


            val typeToViewHolderField = FieldSpec.builder(
                sparseArrayClassType,
                "typeToViewHolder",
                Modifier.PROTECTED
            ).initializer("new \$T<>()", sparseArrayType)
                .build()

            val getTypeToViewHolder =
                MethodSpec.methodBuilder("getTypeToViewHolder")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(sparseArrayClassType)
                    .addStatement("return typeToViewHolder")
                    .build()


            val viewHolderToLayoutResField =
                FieldSpec.builder(
                    arrayMapClassIntType,
                    "viewHolderToLayoutRes",
                    Modifier.PROTECTED
                ).initializer("new \$T<>()", arrayMapType)
                    .build()

            val getViewHolderToLayout =
                MethodSpec.methodBuilder("getViewHolderToLayoutRes")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(arrayMapClassIntType)
                    .addStatement("return viewHolderToLayoutRes")
                    .build()

            // 生成唯一的类名（支持多模块）
            val uniqueClassName = if (moduleIdentifier.isNotEmpty()) {
                "${Constants.CLASS_NAME}_$moduleIdentifier"
            } else {
                Constants.CLASS_NAME
            }
            
            val javaClass = TypeSpec.classBuilder(uniqueClassName)
                .addSuperinterface(ClassName.get(Constants.PACKAGE, "IAdapterMap"))
                .addField(dataToTypeField)
                .addField(typeToViewHolderField)
                .addField(viewHolderToLayoutResField)
                .addMethod(constructor)
                .addMethod(getdataToType)
                .addMethod(getTypeToViewHolder)
                .addMethod(getViewHolderToLayout)
                .addModifiers(Modifier.PUBLIC)
                .build()
            val javaFile = JavaFile.builder(Constants.PACKAGE, javaClass).build()

            try {
                javaFile.writeTo(mFiler)
                log("成功生成适配器映射类: ${Constants.PACKAGE}.$uniqueClassName")
                log("共注册 ${holderClass.size} 个数据类型")
                
                // 生成 ServiceLoader 配置文件
                generateServiceLoaderConfig(uniqueClassName)
            } catch (e: Exception) {
                error("生成代码失败: ${e.message}")
                e.printStackTrace(System.err)
            }
            hasProceed = true
        } catch (e: Exception) {
            error("注解处理过程出错: ${e.message}")
            e.printStackTrace(System.err)
        }
        return false
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(VHLayoutId::class.java.name, VHRef::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}