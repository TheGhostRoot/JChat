package jchat.app_api;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileSystemHandler {
    private final String dir = "attachments/";

    FileSystemHandler() {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            if (!dirFile.mkdir()) {
                API.logger.info("can't make attachments folder!");
            }
        }
    }

    public String generateName(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            List<String> names = new ArrayList<>();
            for (File file : folder.listFiles()) {
                if (file.exists() && file.isFile()) {
                    names.add(file.getName().split("\\.")[0]);
                }
            }
            String g = "";
            while (names.contains(g) || g.isBlank()) {
                g = API.generateKey(50);
            }
            return g;
        }
        return null;
    }

    public boolean saveFile(long user_id, boolean video, String base64, String name) {
        File user_folder = new File(dir + user_id);
        if (!user_folder.exists()) {
            if (!user_folder.mkdirs()) {
                API.logger.info("Can't create user's folder");
                return false;
            }
        }

        File file = new File(user_folder.getAbsolutePath() + "/" + name + (video ? ".mp4" : ".jpg"));
        File videoFile = new File(user_folder.getAbsolutePath() + "/" + name + ".mp4");
        File pictureFile = new File(user_folder.getAbsolutePath() + "/" + name + ".jpg");
        if (videoFile.exists() && !video) {
            deleteFile(user_id, name, false);
        }
        if (pictureFile.exists() && video) {
            deleteFile(user_id, name, true);
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            if (deleteFile(user_id, name, video)) {
                try {
                    if (!file.createNewFile()) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }

            } else {
                return false;
            }
        }

        try {
            Files.write(Paths.get(file.getAbsolutePath()), Base64.decodeBase64(base64));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteFile(long user_id, String name, boolean video) {
        File user_folder = new File(dir + user_id);
        if (user_folder.exists()) {
            File toDelete = new File(user_folder.getAbsolutePath() + "/" + name + (video ? ".mp4" : ".jpg"));
            if (toDelete.exists() && toDelete.isFile()) {
                return toDelete.delete();
            }

            return false;
        }

        return false;
    }



}
