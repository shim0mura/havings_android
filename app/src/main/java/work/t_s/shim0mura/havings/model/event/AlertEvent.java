package work.t_s.shim0mura.havings.model.event;

/**
 * Created by shim0mura on 2015/11/10.
 */
public class AlertEvent {
    public String title;
    public String message;

    public final static int CANT_REACH_SERVER = 0;
    public final static int CANT_PARSE_RESPONSE = 1;
    public final static int SOMETHING_OCCURED_IN_SERVER = 2;



    public AlertEvent(int errorType){
        switch (errorType){
            case CANT_REACH_SERVER:
                this.title = "通信エラー";
                this.message ="通信出来ませんでした。" + "\n" + "電波状態の良いところで再度お試しください。";
                break;
            case SOMETHING_OCCURED_IN_SERVER:
                this.title = "通信エラー";
                this.message = "一時的なエラーが発生しました。" + "\n" + "しばらく時間をおいて再度お試しください。" + "\n" + "しばらく時間をおいても解決しない場合、お問い合わせください。";
                break;
            default:
                this.title = "エラー";
                this.message = "エラーが発生しました。" + "\n" + "エラー詳細は管理者に報告されたので、恐れ入りますが対応完了までしばらくお待ち下さい。";
        }
    }

    public AlertEvent(String title, String message){
        this.title = title;
        this.message = message;
    }
}
