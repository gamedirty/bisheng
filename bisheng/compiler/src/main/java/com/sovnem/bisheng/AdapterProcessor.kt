package com.sovnem.bisheng

import com.squareup.javapoet.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

class AdapterProcessor : AbstractProcessor() {

    lateinit var mFiler: Filer
    lateinit var elementUtil: Elements

    var hasProceed = false
    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
        mFiler = p0.filer
        elementUtil = p0.elementUtils
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
            roundEnvironment.getElementsAnnotatedWith(VHRef::class.java).forEach {
                val an = it.getAnnotation(VHRef::class.java)
                println("蛤？：" + an.lazyLoad)
                if (!an.lazyLoad) {
                    val anno = an.toString()
                    val dataClassName =
                        elementUtil.getPackageOf(it).toString() + "." + it.simpleName
                    holderClass.add(dataClassName)
                    val strs = anno.split(".")
                    val holderName = strs[strs.size - 1].replace(")", "")
                    dataFullToHolderSimple[dataClassName] = holderName
                }

            }
            roundEnvironment.getElementsAnnotatedWith(VHLayoutId::class.java).forEach {
                val ann = it.getAnnotation(VHLayoutId::class.java)
                println("是不是lazy咯啊的L：" + ann.lazyLoad)
                if (!ann.lazyLoad) {
                    val vhClassName = elementUtil.getPackageOf(it).toString() + "." + it.simpleName
                    vhSimpleToFull.put(it.simpleName.toString(), vhClassName)
                    vhResMap.put(vhClassName, it.getAnnotation(VHLayoutId::class.java).layoutId)
                }
            }
            holderClass.forEach {
                dataClassStrToVhRef[it] = vhSimpleToFull[dataFullToHolderSimple[it]]!!
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
            } catch (e: Exception) {
                System.err.println(e.javaClass.name)
                e.printStackTrace(System.err)
            }
            hasProceed = true
        } catch (e: Exception) {
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