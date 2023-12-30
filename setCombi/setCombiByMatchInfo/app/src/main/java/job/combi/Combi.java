package job.combi;

public class Combi {
    int championId;
    String gameVersion;
    String lane;
    int main_perk;
    boolean win;
    int combiId;

    public Combi(int championId, String gameVersion, String lane, int main_perk, boolean win) {
        this.championId = championId;
        this.gameVersion = gameVersion;
        this.lane = lane;
        this.main_perk = main_perk;
        this.win = win;
    }

    public void setCombiId(int combiId) {
        this.combiId = combiId;
    }

    public int championId() {
        return championId;
    }

    public String gameVersion() {
        return gameVersion;
    }

    public String lane() {
        return lane;
    }

    public int main_perk() {
        return main_perk;
    }

    public boolean win() {
        return win;
    }

    public int combiId() {
        return combiId;
    }
}
