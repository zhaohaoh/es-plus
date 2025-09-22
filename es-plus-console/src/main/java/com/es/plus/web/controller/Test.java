//package com.es.plus.web.com.es.plus.samples.controller;
//
//import com.es.plus.web.compile.core.CompilationResult;
//import com.es.plus.web.compile.core.DynamicCodeCompiler;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Map;
//
//public class Test {
//
//    public  EvaluationResult  evaluateSubmission(String submissionId, String code)throws  Exception {
//          // 编译用户代码
//            Map<String, CompilationResult> results = DynamicCodeCompiler.compileMultiple(
//                                            Collections.singletonList(code));
//
//          if  (results ==  null  || results.isEmpty()) {
//                return new EvaluationResult(false,  "Compilation failed");
//            }
//
//          // 创建隔离的类加载环境
//          IsolatedClassLoader isolatedLoader = new IsolatedClassLoader(
//                                            getClass().getClassLoader(),
//                                            Collections.singletonList("java.lang.Math"));
//
//          // 获取编译后的主类
//          String mainClassName=  findMainClass(results);
//            Class<?> mainClass = isolatedLoader.loadClass(mainClassName,
//                                            results.get(mainClassName).getClassBytes());
//
//          // 执行评估
//          try  {
//                Object instance=  mainClass.getDeclaredConstructor().newInstance();
//                Method mainMethod=  mainClass.getMethod("solution",  int[].class);
//
//                // 准备测试用例
//                  List<TestCase> testCases = testCaseRepository.getTestCases(submissionId);
//                  List<TestResult> testResults =  new ArrayList<>();
//
//                // 执行测试
//                for  (TestCase testCase : testCases) {
//                      Object result=  mainMethod.invoke(instance, testCase.getInput());
//                      boolean passed=  verifyResult(result, testCase.getExpectedOutput());
//                        testResults.add(new TestResult(testCase, passed, result));
//                  }
//
//                return new EvaluationResult(true, testResults);
//            }  catch  (Exception e) {
//                return new EvaluationResult(false,  "Execution error: "  + e.getMessage());
//            }
//      }
//
//}
