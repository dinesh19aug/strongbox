package org.carlspring.strongbox.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.carlspring.strongbox.domain.DirectoryContent;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DirectoryContentFetcher
{   
    Logger logger = LoggerFactory.getLogger(DirectoryContent.class);
    
    @Inject
    FileContentFetcher fileContentFetcher;
    
    public DirectoryContent fetchDirectoryContent(Path dirPath)
    {
        DirectoryContent directoryContent = new DirectoryContent();
        
        try 
        {
            List<FileContent> directories = getDirectories(dirPath);
            List<FileContent> files = getFiles(dirPath);
            directoryContent.setDirectories(directories);
            directoryContent.setFiles(files);           
        }
        catch (IOException e)
        {
            logger.debug("Error generating subdirectories and files");
            return null;
        }        
        return directoryContent;
    }
    
    List<FileContent> getDirectories(Path dirPath) throws IOException
    {   
        List<FileContent> directories = new ArrayList<FileContent>();
        
        Files.list(dirPath)
             .filter(p -> !p.getFileName().toString().startsWith("."))
             .filter(p -> {
                try
                {
                    return !Files.isHidden(p);
                }
                catch (IOException e)
                {   
                    logger.debug("Error accessing file");
                    return false;
                }
            })
             .filter(p -> Files.isDirectory(p))
             .sorted()
             .forEach(p -> directories.add(fileContentFetcher.fetchFileContent(p))); 
        
        return directories;
    }
           
    List<FileContent> getFiles(Path dirPath) throws IOException
    {   
        List<FileContent> files = new ArrayList<FileContent>();
        
        Files.list(dirPath)
             .filter(p -> !p.getFileName().toString().startsWith("."))
             .filter(p -> {
                try
                {
                    return !Files.isHidden(p);
                }
                catch (IOException e)
                {
                    logger.debug("Error accessing file");
                    return false;
                }
            })
             .filter(p -> !Files.isDirectory(p))
             .sorted()
             .forEach(p -> files.add(fileContentFetcher.fetchFileContent(p)));  
        
        return files;
    }

    public DirectoryContent fetchDirectoryContent(List<Path> storagePaths)
    {
        DirectoryContent directoryContent = new DirectoryContent();
        
        List<FileContent> directories = new ArrayList<FileContent>();
        List<FileContent> files = new ArrayList<FileContent>();
        
        for(Path path : storagePaths)
        {
            directories.add(fileContentFetcher.fetchFileContent(path));
        }
        
        directoryContent.setDirectories(directories);
        directoryContent.setFiles(files);
        
        return directoryContent;
    }   
}
