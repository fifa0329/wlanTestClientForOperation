package engine;

import java.io.File;



public class DirDel{
	
	public boolean delFile(String path) {
    	File file = new File(path);
    	return file.delete();
    	//return true;
	}
    
    public static void delFolder(String folderPath) {
        try {
                delAllFile(folderPath); //ɾ����������������
                String filePath = folderPath;
                filePath = filePath.toString();
                java.io.File myFilePath = new java.io.File(filePath);
                myFilePath.delete(); //ɾ�����ļ���

        }
        catch (Exception e) {
                System.out.println("ɾ���ļ��в�������");
                e.printStackTrace();

        }
}

    /**
     * ɾ���ļ�������������ļ�
     * @param path String �ļ���·�� �� c:/fqf
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
                return;
        }
        if (!file.isDirectory()) {
       return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
                if (path.endsWith(File.separator)) {
                        temp = new File(path + tempList[i]);
                }
                else {
                        temp = new File(path + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                        temp.delete();
                }
                if (temp.isDirectory()) {
                        delAllFile(path+"/"+ tempList[i]);//��ɾ���ļ���������ļ�
                        delFolder(path+"/"+ tempList[i]);//��ɾ�����ļ���
                }
        }
    }
    }