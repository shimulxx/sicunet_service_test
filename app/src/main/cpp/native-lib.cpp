#include <jni.h>
#include <string>

using namespace std;

extern "C" JNIEXPORT jstring
Java_com_example_sicunetservicetest_MainActivity_stringFromJNI(JNIEnv* env, jobject) {
    string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}