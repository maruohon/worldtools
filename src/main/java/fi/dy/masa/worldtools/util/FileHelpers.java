package fi.dy.masa.worldtools.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import fi.dy.masa.worldtools.WorldTools;

public class FileHelpers
{
    public static File dumpDataToFile(String fileNameBase, List<String> lines)
    {
        File outFile = null;

        File cfgDir = new File(WorldTools.configDirPath);
        if (cfgDir.exists() == false)
        {
            try
            {
                cfgDir.mkdirs();
            }
            catch (Exception e)
            {
                WorldTools.logger.error("dumpDataToFile(): Failed to create the configuration directory.");
                e.printStackTrace();
                return null;
            }

        }

        String fileNameBaseWithDate = fileNameBase + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));
        String fileName = fileNameBaseWithDate + ".txt";
        outFile = new File(cfgDir, fileName);
        int postFix = 1;

        while (outFile.exists() == true)
        {
            fileName = fileNameBaseWithDate + "_" + postFix + ".txt";
            outFile = new File(cfgDir, fileName);
            postFix++;
        }

        try
        {
            outFile.createNewFile();
        }
        catch (IOException e)
        {
            WorldTools.logger.error("dumpDataToFile(): Failed to create data dump file '" + fileName + "'");
            e.printStackTrace();
            return null;
        }

        try
        {
            for (int i = 0; i < lines.size(); ++i)
            {
                FileUtils.writeStringToFile(outFile, lines.get(i) + System.getProperty("line.separator"), true);
            }
        }
        catch (IOException e)
        {
            WorldTools.logger.error("dumpDataToFile(): Exception while writing data dump to file '" + fileName + "'");
            e.printStackTrace();
        }

        return outFile;
    }
}
