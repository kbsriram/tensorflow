# Description:
# TensorFlow Java API.

package(default_visibility = ["//visibility:private"])

licenses(["notice"])  # Apache 2.0

load("build_defs", "JAVACOPTS", "GenTestRules")

java_library(
    name = "tensorflow",
    srcs = [
        ":java_op_sources",
        ":java_sources",
    ],
    data = [":libtensorflow_jni"],
    javacopts = JAVACOPTS,
    visibility = ["//visibility:public"],
    deps = ["@errorprone_annotations"],
)

# NOTE(ashankar): Rule to include the Java API in the Android Inference Library
# .aar. At some point, might make sense for a .aar rule here instead.
filegroup(
    name = "java_sources",
    srcs = glob(["src/main/java/org/tensorflow/*.java"]),
    visibility = [
        "//tensorflow/contrib/android:__pkg__",
        "//tensorflow/java:__pkg__",
    ],
)

filegroup(
    name = "java_op_sources",
    srcs = glob(["src/main/java/org/tensorflow/op/*.java"]),
    visibility = [
        "//tensorflow/java:__pkg__",
    ],
)

java_library(
    name = "testutil",
    testonly = 1,
    srcs = ["src/test/java/org/tensorflow/TestUtil.java"],
    javacopts = JAVACOPTS,
    deps = [":tensorflow"],
)

GenTestRules(
    name = "GeneratedTestRules",
    exclude_tests = ["src/test/java/org/tensorflow/SavedModelBundleTest"],
    javacopts = JAVACOPTS,
    test_files = glob([
        "src/test/java/org/tensorflow/*Test.java",
        "src/test/java/org/tensorflow/op/*Test.java",
    ]),
    deps = [
        ":tensorflow",
        ":testutil",
        "@junit",
    ],
)

java_test(
    name = "SavedModelBundleTest",
    size = "small",
    srcs = ["src/test/java/org/tensorflow/SavedModelBundleTest.java"],
    data = ["//tensorflow/cc/saved_model:saved_model_half_plus_two"],
    javacopts = JAVACOPTS,
    test_class = "org.tensorflow.SavedModelBundleTest",
    deps = [
        ":tensorflow",
        ":testutil",
        "@junit",
    ],
)

filegroup(
    name = "libtensorflow_jni",
    srcs = select({
        "//tensorflow:darwin": [":libtensorflow_jni.dylib"],
        "//conditions:default": [":libtensorflow_jni.so"],
    }),
    visibility = ["//visibility:public"],
)

LINKER_VERSION_SCRIPT = ":config/version_script.lds"

LINKER_EXPORTED_SYMBOLS = ":config/exported_symbols.lds"

cc_binary(
    name = "libtensorflow_jni.so",
    # Set linker options to strip out anything except the JNI
    # symbols from the library. This reduces the size of the library
    # considerably (~50% as of January 2017).
    linkopts = select({
        "//tensorflow:debug": [],  # Disable all custom linker options in debug mode
        "//tensorflow:darwin": [
            "-Wl,-exported_symbols_list",  # This line must be directly followed by LINKER_EXPORTED_SYMBOLS
            LINKER_EXPORTED_SYMBOLS,
        ],
        "//tensorflow:windows": [],
        "//tensorflow:windows_msvc": [],
        "//conditions:default": [
            "-z defs",
            "-s",
            "-Wl,--version-script",  #  This line must be directly followed by LINKER_VERSION_SCRIPT
            LINKER_VERSION_SCRIPT,
        ],
    }),
    linkshared = 1,
    linkstatic = 1,
    deps = [
        "//tensorflow/java/src/main/native",
        LINKER_VERSION_SCRIPT,
        LINKER_EXPORTED_SYMBOLS,
    ],
)

genrule(
    name = "pom",
    outs = ["pom.xml"],
    cmd = "$(location generate_pom) >$@",
    output_to_bindir = 1,
    tools = [":generate_pom"],
)

cc_binary(
    name = "generate_pom",
    srcs = ["generate_pom.cc"],
    deps = ["//tensorflow/c:c_api"],
)

# System.loadLibrary() on OS X looks for ".dylib" or ".jnilib"
# and no ".so". If and when https://github.com/bazelbuild/bazel/issues/914
# is resolved, perhaps this workaround rule can be removed.
genrule(
    name = "darwin-compat",
    srcs = [":libtensorflow_jni.so"],
    outs = ["libtensorflow_jni.dylib"],
    cmd = "cp $< $@",
    output_to_bindir = 1,
)

filegroup(
    name = "all_files",
    srcs = glob(
        ["**/*"],
        exclude = [
            "**/METADATA",
            "**/OWNERS",
        ],
    ),
    visibility = ["//tensorflow:__subpackages__"],
)
