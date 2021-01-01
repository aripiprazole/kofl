package com.lorenzoog.kofl.dslgenerator

import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
fun main() {
  val builderAnnotationName = "DescriptorBuilder"
  val currentPackage = "com.lorenzoog.kofl.compiler.common.backend"
  val file = File("compiler.common/common/backend/Descriptor.kt")
  val target = File("compiler.common/common/backend/Builders.kt")
  // todo only get annotated elements
  val excluded = listOf("MutableDescriptor", "NativeDescriptor", "CallableDescriptor")

  if (target.isDirectory) target.delete()

  if (!target.exists()) target.createNewFile()

  val environment = KotlinCoreEnvironment
    .createForProduction(
      Disposer.newDisposable(),
      CompilerConfiguration().apply {
        put(
          CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
          PrintingMessageCollector(System.err, MessageRenderer.GRADLE_STYLE, true)
        )
      },
      EnvironmentConfigFiles.JVM_CONFIG_FILES
    )

  val ktFile = PsiManager.getInstance(environment.project).findFile(
    LightVirtualFile("temp.kt", KotlinFileType.INSTANCE, file.readText())
  ) as KtFile? ?: error("File is null")

  val imports = ktFile.children.filterIsInstance<KtImportList>()
    .flatMap { it.imports }
    .map { it.importPath.toString() }

  fun findImportPackage(name: String): String {
    return when (val packageName = imports.find { it.split(".").lastOrNull() == name }) {
      is String -> packageName.split(".").let { it.take(it.size - 1).joinToString(".") }
      else -> currentPackage
    }
  }

  val descriptors = buildMap<String, TypeSpec> {
    ktFile.children
      .filterIsInstance<KtClass>()
      .filter { it.isData() && !it.isSealed() && !it.isAbstract() && !it.isInterface() }
      .filter { it.name?.endsWith("Descriptor") ?: error("$it should have a name!") }
      .filter { it.name !in excluded }
      .forEach { ktClass ->
        val name = ktClass.name ?: error("$ktClass should have a name!")
        val constructor = ktClass.primaryConstructor ?: error("$ktClass should have a constructor!")

        set(name, TypeSpec.classBuilder(name).apply {
          primaryConstructor(FunSpec.constructorBuilder().apply {
            constructor.valueParameters.forEach parameterIter@{ parameter ->
              val parameterName = parameter.name ?: error("$parameter should have a name!")

              addParameter(parameterName, parameter.text
                .also { println("TEXT: $it") }
                .split(":").getOrNull(1).let { it ?: error("parameter $parameterName should have a type!") }
                .let mapping@{ typeName ->
                  fun findTypeRecursive(name: String, recursive: Boolean = false): TypeName {
                    val realName = if ("<" in name) {
                      name.substringBefore("<")
                    } else {
                      name
                    }

                    println("RECURSIVE: $recursive")
                    println("NAME: $name")
                    println("REAL NAME: $realName")

                    val type = ClassName(findImportPackage(realName), realName)

                    if ("<" in name) {
                      val parameters = name.substringAfter("<").substringBefore(">").split(",").map { it.trim() }

                      println("PARAMETERS: $parameters")
                      println()

                      return type.parameterizedBy(parameters.map {
                        findTypeRecursive(it, true)
                      })
                    }

                    println()

                    return type
                  }

                  findTypeRecursive(typeName.trim())
                })
            }
          }.build())
        }.build())

      }
  }

  target.writeText(FileSpec.builder(currentPackage, "Builders").apply {
    addType(
      TypeSpec.annotationBuilder("DescriptorDsl")
        .addAnnotation(ClassName("kotlin", "DslMarker"))
        .build()
    )

    addAnnotation(
      AnnotationSpec.builder(Suppress::class)
        .addMember("%S, %S", "unused", "MemberVisibilityCanBePrivate")
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
        addModifiers(KModifier.INLINE)
        addAnnotation(ClassName(currentPackage, "DescriptorDsl"))

        returns(returnType)

        addParameter("builder", LambdaTypeName.get(builderType, returnType = UNIT))
        addStatement("return %T().apply(builder).build()", builderType)
      }.build())
    }
  }.build().toString())
}