/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dirbaio.nsmbe;

import java.io.IOException;
import net.dirbaio.nsmbe.fs.NitroROMFilesystem;

/**
 *
 * @author dirbaio
 */
public class NSMBe
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            NitroROMFilesystem fs = new NitroROMFilesystem("nsmb.nds");
            
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
}
