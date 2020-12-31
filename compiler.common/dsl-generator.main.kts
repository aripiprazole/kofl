@file:OptIn(ExperimentalStdlibApi::class)
@file:DependsOn("com.squareup:kotlinpoet:1.6.0")

import com.squareup.kotlinpoet.*

val currentPackage = "com.lorenzoog.kofl.compiler.common.backend"

val koflType = ClassName("com.lorenzoog.kofl.compiler.common.typing", "KoflType")

val descriptors = buildMap<String, TypeSpec> {
  // TODO: read source code from Descriptor.kt
  set("ConstDescriptor", TypeSpec.classBuilder("ConstDescriptor").apply {
    primaryConstructor(FunSpec.constructorBuilder().apply {
      addParameter("value", ANY)
      addParameter("type", koflType)
      addParameter("line", INT)
    }.build())
  }.build())
}

val generated = FileSpec.builder(currentPackage, "Builders").apply {
  addAnnotation(
    AnnotationSpec.builder(Suppress::class)
      .addMember("%S", "unused")
      .build()
  )

  descriptors.forEach { (name, type) ->
    val returnType = ClassName(currentPackage, type.name ?: error("$type should have a name!"))
    val builderClass = TypeSpec.classBuilder(name + "Builder").apply {
      val constructor = type.primaryConstructor ?: error("A descriptor should have a primary constructor!")

      constructor.parameters.forEach { property ->
        addProperty(
          PropertySpec.builder(property.name, property.type.copy(nullable = true))
            .initializer("null")
            .mutable()
            .build()
        )
      }

      addFunction(FunSpec.builder("build").apply {
        returns(returnType)

        addStatement(buildString {
          append("return %T(")
          constructor.parameters.forEach { property ->
            append("${property.name}!!, ")
          }
          append(")")
        }, returnType)
      }.build())
    }.build()

    val builderType = ClassName(currentPackage, builderClass.name!!)

    addType(builderClass)
    addFunction(FunSpec.builder(name.decapitalize()).apply {
      returns(returnType)

      addParameter("builder", LambdaTypeName.get(builderType, returnType = UNIT))
      addStatement("return %T().apply(builder).build()", builderType)
    }.build())
  }
}.build()

println(generated.toString())