package com.sovnem.bisheng

import com.squareup.javapoet.*
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

class AdapterProcessor : AbstractProcessor() {

    private lateinit var mFiler: Filer
    private lateinit var elementUtil: Elements
    private lateinit var messager: Messager

    private var hasProceed = false
    
    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
        mFiler = p0.filer
        elementUtil = p0.elementUtils
        messager = p0.messager
        log("BiSheng注解处理器初始化完成")
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
            val constructor =
                MethodSpec.constructorBuilder()
                    .apply {
                        holderClass.forEach {
                            val vhFullName = dataClassStrToVhRef[it]
                            val resId = vhResMap[vhFullName]
                            addStatement(
                                "dataToType.put($it.class,$it.class.hashCode())"
                            )
                            addStatement(
                                "typeToViewHolder.put($it.class.hashCode(), $vhFullName.class)"
                            )
                            addStatement("viewHolderToLayoutRes.put($vhFullName.class,$resId)")
                        }
                    }.addModifiers(Modifier.PUBLIC).build()

            val getdataToType =
                MethodSpec.methodBuilder("getDataToType").addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC).returns(Class.forName("android.util.ArrayMap"))
                    .addStatement("return dataToType").build()

            val dataToTypeField = FieldSpec.builder(
                Class.forName("android.util.ArrayMap"),
                "dataToType",
                Modifier.PROTECTED
            ).initializer(CodeBlock.of("new ArrayMap<Class<?>, Integer>()")).build()


            val typeToViewHolderField = FieldSpec.builder(
                Class.forName("android.util.SparseArray"),
                "typeToViewHolder",
                Modifier.PROTECTED
            ).initializer(CodeBlock.of("new SparseArray<Class<?>>()"))
                .build()

            val getTypeToViewHolder =
                MethodSpec.methodBuilder("getTypeToViewHolder").addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(Class.forName("android.util.SparseArray"))
                    .addStatement("return typeToViewHolder").build()


            val viewHolderToLayoutResField =
                FieldSpec.builder(
                    Class.forName("android.util.ArrayMap"),
                    "viewHolderToLayoutRes",
                    Modifier.PROTECTED
                ).initializer(CodeBlock.of("new ArrayMap<Class<?>, Integer>()"))
                    .build()

            val getViewHolderToLayout =
                MethodSpec.methodBuilder("getViewHolderToLayoutRes")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC).returns(Class.forName("android.util.ArrayMap"))
                    .addStatement("return viewHolderToLayoutRes").build()

            val javaClass = TypeSpec.classBuilder(Constants.CLASS_NAME)
                .addSuperinterface(Class.forName(Constants.SUPER_ADAPTER_CLASS))
                .addField(dataToTypeField)
                .addField(typeToViewHolderField)
                .addMethod(getdataToType)
                .addMethod(getTypeToViewHolder)
                .addMethod(getViewHolderToLayout)
                .addField(viewHolderToLayoutResField)
                .addModifiers(Modifier.PUBLIC).addMethod(constructor).build()
            val javaFile = JavaFile.builder(Constants.PACKAGE, javaClass).build()

            try {
                javaFile.writeTo(mFiler)
                log("成功生成适配器映射类: ${Constants.PACKAGE}.${Constants.CLASS_NAME}")
                log("共注册 ${holderClass.size} 个数据类型")
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