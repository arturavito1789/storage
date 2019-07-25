import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Consumer;

public class Handler extends HandlerMessageBase {

    private Consumer<String> errorLoginToServer;
    private Consumer<List<String>> showStageWork;
    private UpdateCustomList addItemlistServer;
    private UpdateCustomList deleteItemlistServer;
    private UpdateCustomList addItemlistClient;
    private DeleteCustomList clearlistClient;
    private Consumer<Boolean>  clearlistServer;
    public boolean goTransferListFile;

    public Handler(Consumer<String> errorLoginToServer,
                   Consumer<List<String>> showStageWork,
                   UpdateCustomList addItemlistServer,
                   UpdateCustomList deleteItemlistServer,
                   UpdateCustomList addItemlistClient,
                   DeleteCustomList clearlistClient,
                   Consumer<Boolean> clearlistServer) {
        this.errorLoginToServer = errorLoginToServer;
        this.showStageWork = showStageWork;
        this.addItemlistServer = addItemlistServer;
        this.deleteItemlistServer = deleteItemlistServer;
        this.addItemlistClient = addItemlistClient;
        this.clearlistClient = clearlistClient;
        this.clearlistServer = clearlistServer;
        this.goTransferListFile = false;
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        this.mainPath = s.substring(0, s.lastIndexOf("cloud")+5) + "\\" + "klient file" + "\\";
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (commandFileTransfer == null) {
            switch (((Command) msg).typeCommand) {
                case "LoginToServer":
                    CommandLoginToServer commandLoginToServer = (CommandLoginToServer) msg;
                    if (commandLoginToServer.isError() == true) {
                        errorLoginToServer.accept(commandLoginToServer.getCause());
                    }
                    else {
                        //авторизация либо регистрация прошла успешна показываем вторую сцену
                        showStageWork.accept(commandLoginToServer.getListFile());
                    }
                    break;
                case "CommandResponseDeleteNameFile":
                    CommandResponseDeleteNameFile commandResponseDeleteNameFile = (CommandResponseDeleteNameFile) msg;
                    deleteItemlistServer.customFunction(commandResponseDeleteNameFile.getName());
                    break;
                case "CommandResponseAddNameFile":
                    //если переносим список файлов то обновляем интерфейс после того как последний файл будет отослан и
                    //придет от сервера потверждение о его получении
                    if (goTransferListFile == true){
                        if(listNameFileTransfer.size()>0){
                           String nameF = listNameFileTransfer.get(0);
                           listNameFileTransfer.remove(0);
                           sendFile(nameF);
                        }else{
                           goTransferListFile = false;
                           clearlistServer.accept(false);
                        }
                    }else {
                        CommandResponseAddNameFile commandResponseAddNameFile = (CommandResponseAddNameFile) msg;
                        addItemlistServer.customFunction(commandResponseAddNameFile.name);
                    }
                    break;
                case "CommandFileInfo":
                    ctx.channel().attr(fileTransfer).set(true);
                    CommandFileInfo commandFileInfo = (CommandFileInfo) msg;
                    commandFileTransfer = new CommandFileInfo(commandFileInfo.name, commandFileInfo.len);
                    break;
                case "CommandResponseAllFileServer":
                    CommandResponseAllFileServer commandResponseAllFileServer = (CommandResponseAllFileServer) msg;
                    if (commandResponseAllFileServer.error == true){
                        clearlistServer.accept(true);
                    }else{
                        Path path = Paths.get(mainPath);
                        try {
                            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    if (!file.toFile().isDirectory()){
                                        String fileName = file.getFileName().toString();
                                        listNameFileTransfer.add(fileName);
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //отсылаем файлы по одному
                        goTransferListFile = true;
                        String nameF = listNameFileTransfer.get(0);
                        listNameFileTransfer.remove(0);
                        sendFile(nameF);
                    }
                    break;
                case "CommandResponseAllClient":
                    goTransferListFile = false;
                    clearlistClient.customFunction();
            }

        } else {
            ByteBuf buf = ((ByteBuf) msg);
            try {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(mainPath + commandFileTransfer.name, true));
                int i = buf.readableBytes();
                countByteFile = countByteFile + i;
                while (i  > 0) {
                    out.write(buf.readByte());
                    i = buf.readableBytes();
                }
                out.close();

                if (commandFileTransfer.len == countByteFile){
                    ctx.channel().attr(fileTransfer).set(false);
                    countByteFile =0;
                    if (goTransferListFile == false) {
                        addItemlistClient.customFunction(commandFileTransfer.name);
                    }
                    commandFileTransfer  = null;
                    if (goTransferListFile == true) {
                        CommandResponseAddNameFile commandResponseAddNameFile = new CommandResponseAddNameFile();
                        sendCommand(commandResponseAddNameFile);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            buf.release(); // освобождаем буфер удаляем все ссылки на него.
        }
    }
}
