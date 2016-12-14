package com.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;

/**
 * Created by jiuyun.zhang on 2016/12/5.
 */
public class LoadedAgent {

    private static Class loadedAgent = LoadedAgent.class;

    private static Instrumentation inst;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        inst = instrumentation;
    }

    /** 类加载调用 */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        inst = instrumentation;
    }

    public static Instrumentation instrumentation() {
        return inst;
    }

    public static void start() {
        load();
        inst.addTransformer(new HelloTransformer(), true);
        Class[] classes = inst.getAllLoadedClasses();
        try {
            for (Class clazz : classes) {
                String clsName = clazz.getName();
                if (clsName.equals("com.test.Test")) {
                    System.out.println(clsName);
                    inst.retransformClasses(clazz);
                }
            }
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }

    private static boolean load() {

        String pid = getPid();

        boolean succ = false;
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            String agentPath = getAgentPath();
            vm.loadAgent(agentPath);
            succ = true;
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AgentInitializationException e) {
            e.printStackTrace();
        } catch (AgentLoadException e) {
            e.printStackTrace();
        }
        return succ;
    }

    private static String getAgentPath() {
        String agentPath = loadedAgent.getProtectionDomain()
                .getCodeSource().getLocation().getPath();

        return agentPath;
    }

    private static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];

        return pid;
    }

}
