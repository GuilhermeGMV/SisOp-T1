public class AllocResult {
    public boolean success;
    public int[] frames;

    public AllocResult(boolean success, int[] frames) {
        this.success = success;
        this.frames = frames;
    }
}