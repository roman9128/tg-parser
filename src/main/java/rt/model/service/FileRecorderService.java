package rt.model.service;

public interface FileRecorderService {
    void write(ResponsePrinter printer, boolean writeAll);
}