public abstract class SpecificationManager extends SpecificationClass {
    private static SpecificationClass dbExporter;

    public static void registerExporter(SpecificationClass dbExp) {
        dbExporter = dbExp;
    }

    public static SpecificationClass getExporter(String fileName) {
        dbExporter.setFileName(fileName);
        return dbExporter;
    }
}
