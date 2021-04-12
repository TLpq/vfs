package vip.smartfamily.vfs;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class TestVFS {
    public static void main(String[] args) {
        SMBClient client = new SMBClient();
        try (Connection connection = client.connect("192.168.101.15")) {
            AuthenticationContext ac = new AuthenticationContext("SUMBUM", "zdzdzdzd".toCharArray(), "192.168.101.15");
            Session session = connection.authenticate(ac);

            try {
                // Connect to Share
                DiskShare share = (DiskShare) session.connectShare("yf");
                for (FileIdBothDirectoryInformation f : share.list("", "*")) {
                    if (f.getFileAttributes() == FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) {
                        String name = f.getFileName();
                        if (!".".equals(name) && !"..".equals(name)) {
                            // 打开文件夹
                            Directory directory = share.openDirectory(
                                    name,
                                    new HashSet<>(Collections.singletonList(AccessMask.GENERIC_ALL)),
                                    new HashSet<>(Collections.singletonList(FileAttributes.FILE_ATTRIBUTE_NORMAL)),
                                    SMB2ShareAccess.ALL,
                                    SMB2CreateDisposition.FILE_OPEN,
                                    new HashSet<>(Collections.singletonList(SMB2CreateOptions.FILE_DIRECTORY_FILE)));

                            for (FileIdBothDirectoryInformation w : directory.list()) {
                                System.out.println("File : " + w.getFileName());
                                System.out.println("FileType : " + w.getFileAttributes());
                                System.out.println("FileShortName : " + w.getShortName());
                                System.out.println("=========================================");
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
