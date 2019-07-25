import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;

public class Handler extends HandlerMessageBase{

    private Dao dao;
    private Server server;
    private boolean isWork;


    public Handler(Server server) {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        this.mainPath = s.substring(0, s.lastIndexOf("cloud")+5) + "\\" + "server file" + "\\";
        this.dao = new Dao();
        this.server = server;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //ошибка может возникнуть еще до тго как мы добавили текушее подключение в список обслуживаемых сервером
        if (isWork == true){
            isWork = false;
            server.dicrement();
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (commandFileTransfer == null) {
            boolean rez = false;
            CommandLoginToServer commandLoginToServer = new CommandLoginToServer();
            String nameUser = "";
            Path path = null;
            switch (((Command) msg).typeCommand) {
                case "Registration":
                    //проверяем если пользователь с таким именем
                    CommandRegistration commandRegistration = (CommandRegistration) msg;
                    nameUser = commandRegistration.getUsername();
                    rez = dao.findNameUsers(nameUser);
                    if(rez == true){
                        commandLoginToServer.setError(true);
                        commandLoginToServer.setCause("пользователь с таким именем уже существует");
                    }
                    else{
                        rez = dao.createUser(nameUser,commandRegistration.getPassword());
                        if(rez == true){
                            commandLoginToServer.setCause("операция прошла успешно");
                        }
                        else{
                            commandLoginToServer.setError(true);
                            commandLoginToServer.setCause("не удалось записать дданные о пользователе в базу данных");
                        }
                    }
                    rez = sendCommand(commandLoginToServer);
                    if(rez==true && commandLoginToServer.isError() == false){
                        //обновляем счетчик подключений. Создаем папку для файлов пользователя
                        mainPath = mainPath + nameUser;
                        File theDir = new File(mainPath);
                        mainPath = mainPath + "//";
                        theDir.mkdir();
                        isWork = true;
                        server.increment();
                    }
                    if(rez==true && commandLoginToServer.isError() == true){
                        //отключаемся при новом подключении пользователя будет создан новый обработчик
                        ctxActiv.close();
                    }
                    break;
                case "Authorization":
                    CommandAuthorization commandAuthorization = (CommandAuthorization) msg;
                    nameUser = commandAuthorization.getUsername();
                    rez = dao.findNameByPassUsers(nameUser, commandAuthorization.getPassword());
                    if(rez == true){
                        commandLoginToServer.setCause("операция прошла успешно");
                        mainPath = mainPath + nameUser;
                        List<String> listFileServer = new ArrayList<String>();
                        path = Paths.get(mainPath);
                        mainPath = mainPath + "//";
                        try {
                            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    String fileName = file.getFileName().toString();
                                    if (!file.toFile().isDirectory()){
                                        listFileServer.add(fileName);
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        commandLoginToServer.setListFile(listFileServer);
                    }
                    else{
                        commandLoginToServer.setError(true);
                        commandLoginToServer.setCause("не верно имя пользователя или пароль");
                    }
                    rez = sendCommand(commandLoginToServer);
                    if(rez==true && commandLoginToServer.isError() == false){
                        //обновляем счетчик подключений
                        isWork = true;
                        server.increment();
                    }
                    if(rez==true && commandLoginToServer.isError() == true){
                        //отключаемся. при новом подключении пользователя будет создан новый обработчик
                        ctxActiv.close();
                    }
                    break;
                case "CommandReceiveDeleteNameFile":
                    CommandReceiveDeleteNameFile commandReceiveDeleteNameFile = (CommandReceiveDeleteNameFile) msg;
                    File file = new File(mainPath  + commandReceiveDeleteNameFile.name);
                    file.delete();
                    CommandResponseDeleteNameFile commandResponseDeleteNameFile = new CommandResponseDeleteNameFile();
                    commandResponseDeleteNameFile.setName(commandReceiveDeleteNameFile.name);
                    rez = sendCommand(commandResponseDeleteNameFile);
                    break;
                case "CommandReceiveNameFile":
                    CommandReceiveNameFile сommandReceiveNameFile = (CommandReceiveNameFile) msg;
                    //1) отсылаем CommandFileInfo потом сам файл
                    sendFile(сommandReceiveNameFile.name);
                    break;
                case "CommandFileInfo":
                    ctx.channel().attr(fileTransfer).set(true);
                    CommandFileInfo commandFileInfo = (CommandFileInfo) msg;
                    commandFileTransfer = new CommandFileInfo(commandFileInfo.name, commandFileInfo.len);
                    break;
                case "CommandReceiveAllFileServer":
                    CommandResponseAllFileServer commandResponseAllFileServer  = new CommandResponseAllFileServer();
                    //удаляем все файлы с сервера
                    path = Paths.get(mainPath);
                    try {
                        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (!file.toFile().isDirectory()){
                                    File fileD = file.toFile();
                                    fileD.delete();
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        commandResponseAllFileServer.error = true;
                    }
                    sendCommand(commandResponseAllFileServer);
                    break;
                case "CommandReceiveAllClient":
                    path = Paths.get(mainPath);
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
                    String nameF = listNameFileTransfer.get(0);
                    listNameFileTransfer.remove(0);
                    sendFile(nameF);
                    break;
                case "CommandResponseAddNameFile":
                    if(listNameFileTransfer.size()>0){
                        String nameFil = listNameFileTransfer.get(0);
                        listNameFileTransfer.remove(0);
                        sendFile(nameFil);
                    }else{
                        CommandResponseAllClient commandResponseAllClient = new CommandResponseAllClient();
                        sendCommand(commandResponseAllClient);
                    }

                    break;
            }

        }else{
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
                    CommandResponseAddNameFile commandResponseAddNameFile = new CommandResponseAddNameFile();
                    commandResponseAddNameFile.name = commandFileTransfer.name;
                    commandFileTransfer  = null;
                    sendCommand(commandResponseAddNameFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            buf.release(); // освобождаем буфер удаляем все ссылки на него.
        }

    }


}
