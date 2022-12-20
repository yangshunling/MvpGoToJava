package com.mvpgo.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.mvpgo.plugin.FileUtil;

import java.io.*;

public class MvpAction extends AnAction {

    Project project;
    VirtualFile selectGroup;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取当前事件的文件对象
        selectGroup = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        // 获取当前文件对象
        project = e.getData(PlatformDataKeys.PROJECT);
        // 创建对话框
        String className = Messages.showInputDialog(project, "请输入类名称", "NewMvpGroup", Messages.getQuestionIcon());
        if (className == null || className.equals("")) {
            Messages.showErrorDialog(project, "请输入类名", "NewMvpGroup");
            return;
        }
        if (className.equals("mvp")) {
            // 生成mvp框架代码
            createMvpClass();
        } else if (className.equals("base")) {
            createBaseClass();
        } else {
            // 生成模块代码
            createMoudleClass(className);
        }
        // 刷新文件目录
        project.getProjectFile().refresh(false, true);
    }

    /**
     * 创建MVP架构
     */
    private void createMvpClass() {
        // 设置生成路径
        String path = selectGroup.getPath() + "/mvp";
        String packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");
        // 读取模板文件
        String presenter = readFile("BasePresenter.txt").replace("&package&", packageName);
        String presenterImpl = readFile("BasePresenterImpl.txt").replace("&package&", packageName);
        String view = readFile("BaseView.txt").replace("&package&", packageName);
        String activity = readFile("MVPBaseActivity.txt").replace("&package&", packageName);
        String fragment = readFile("MVPBaseFragment.txt").replace("&package&", packageName);
        // 代码写入
        writeToFile(presenter, path, "BasePresenter.java");
        writeToFile(presenterImpl, path, "BasePresenterImpl.java");
        writeToFile(view, path, "BaseView.java");
        writeToFile(activity, path, "MVPBaseActivity.java");
        writeToFile(fragment, path, "MVPBaseFragment.java");

    }

    /**
     * 创建Base架构
     */
    private void createBaseClass() {
    }

    /**
     * 创建模块代码
     */
    private void createMoudleClass(String className) {
        boolean isFragment = className.endsWith("Fragment") || className.endsWith("fragment");
        if (className.endsWith("Fragment") || className.endsWith("fragment") || className.endsWith("Activity") || className.endsWith("activity")) {
            className = className.substring(0, className.length() - 8);
        }
        String path = selectGroup.getPath() + "/" + className.toLowerCase();
        String packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");
        String mvpPath = FileUtil.traverseFolder(path.substring(0, path.indexOf("java")));
        mvpPath = mvpPath.substring(mvpPath.indexOf("java") + 5, mvpPath.length()).replace("/", ".").replace("\\", ".");
        className = className.substring(0, 1).toUpperCase() + className.substring(1);
        String contract = readFile("Contract.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Contract&", className + "Contract");
        String presenter = readFile("Presenter.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Contract&", className + "Contract").replace("&Presenter&", className + "Presenter");
        if (isFragment) {
            String fragment = readFile("Fragment.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Fragment&", className + "Fragment").replace("&Contract&", className + "Contract").replace("&Presenter&", className + "Presenter");
            writeToFile(fragment, path, className + "Fragment.java");
        } else {
            String activity = readFile("Activity.txt").replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Activity&", className + "Activity").replace("&Contract&", className + "Contract").replace("&Presenter&", className + "Presenter");
            writeToFile(activity, path, className + "Activity.java");
        }
        writeToFile(contract, path, className + "Contract.java");
        writeToFile(presenter, path, className + "Presenter.java");
    }

    private String readFile(String filename) {
        InputStream in = this.getClass().getResourceAsStream("template/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    private void writeToFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }
        } catch (IOException e) {

        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }
}
