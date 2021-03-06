package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominik Grzelak
 * @since 16.07.2017.
 */
public class FileCreateUtil {

    public static PsiElement createFile(String className, @NotNull PsiDirectory directory, final String templateName, AnAction actionClass) throws Exception {
        final Properties props = new Properties(FileTemplateManager.getInstance(directory.getProject()).getDefaultProperties());
        props.setProperty(FileTemplate.ATTRIBUTE_NAME, className);

        final FileTemplate template = FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(templateName);

        return FileTemplateUtil.createFromTemplate(template, className, props, directory, actionClass.getClass().getClassLoader());
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static File findParentLike(String pattern, File dir, File limit) {
        String[] names = pattern.split("/");
        Collections.reverse(Arrays.asList(names));

        while (dir != null && dir.getPath().startsWith(limit.getPath())) {
            if (names[0].equals(dir.getName())) {
                if (checkParents(dir, names)) {
                    return dir;
                }
            }

            dir = dir.getParentFile();
        }
        return null;
    }


    public static boolean checkParents(File dir, String[] names) {
        for (String name : names) {
            if (dir.getName().equals(name)) {
                dir = dir.getParentFile();
            } else {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static VirtualFile getParentDirectory(VirtualFile src, String dirName) {
        if (src.getParent().isDirectory() && src.getParent().getName().equals(dirName)) {
            return src.getParent();
        }
        return getParentDirectory(src.getParent(), dirName);
    }

    public static VirtualFile getChildDirectory(VirtualFile src, String dirName) {
        String[] split = dirName.split("/");
        if (split.length > 0 && !split[0].isEmpty()) {
            VirtualFile child = src.findChild(split[0]);
            if (Objects.nonNull(child)) {
                String collect = Arrays.stream(split).skip(1).filter(Objects::nonNull).collect(Collectors.joining("/"));
                return getChildDirectory(child, collect);
            }
        }
        return src;
    }

    //https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000080064-find-virtual-file-for-relative-path-under-content-roots
    public static List<VirtualFile> findFileByRelativePath(@NotNull Project project, @NotNull String fileRelativePath) {
        String relativePath = fileRelativePath.startsWith("/") ? fileRelativePath : "/" + fileRelativePath;
        FileType fileTypes = FileTypeManager.getInstance().getFileTypeByFileName(relativePath);
        final List<VirtualFile> fileList = new ArrayList<>();
//        Collection<VirtualFile> files = FileTypeIndex.getFiles(fileTypes, GlobalSearchScope.projectScope(project));
        FileTypeIndex.processFiles(fileTypes, new Processor<VirtualFile>() {
            @Override
            public boolean process(VirtualFile virtualFile) {
                if (virtualFile.getPath().endsWith(relativePath)) {
                    fileList.add(virtualFile);
                }
                return true;
            }
        }, GlobalSearchScope.projectScope(project));
//        FileBasedIndex.getInstance().processFilesContainingAllKeys(FileTypeIndex.NAME, fileTypes, GlobalSearchScope.projectScope(project), null, virtualFile -> {
//            if (virtualFile.getPath().endsWith(relativePath)) {
//                fileList.add(virtualFile);
//            }
//            return true;
//        });
        return fileList;
    }
}
