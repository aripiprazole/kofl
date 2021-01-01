rootProject.name = "kfl"
enableFeaturePreview("GRADLE_METADATA")
include("interpreter")
include("frontend")
include("compiler.common")
include("compiler.vm")
include("compiler.common:dsl-generator")
