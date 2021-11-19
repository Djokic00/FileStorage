public abstract class SpecificationManager {
    private static SpecificationClass exporter;

    public static void registerExporter(SpecificationClass exp) {
        exporter = exp;
    }

    public static SpecificationClass getExporter(String fileName) {
        exporter.setFileName(fileName);
        return exporter;
    }
}
