package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.CommentEntity;
import work.t_s.shim0mura.havings.presenter.CommentPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2016/01/26.
 */
public class CommentAdapter extends ArrayAdapter<CommentEntity> {

    private final int MAX_DELETE_PROMPT = 30;
    Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    private List<CommentEntity> commentEntityList;
    private CommentPresenter commentPresenter;

    public CommentAdapter(Context context, int resource, List<CommentEntity> comments, CommentPresenter cp) {
        super(context, resource);
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        commentEntityList = comments;
        commentPresenter = cp;
    }

    @Override
    public int getCount() {
        return commentEntityList.size();
    }

    @Override
    public CommentEntity getItem(int position) {
        return commentEntityList.get(position);
    }

    public void addComment(CommentEntity commentEntity){
        commentEntityList.add(commentEntity);
        notifyDataSetChanged();
    }

    public void removeComment(CommentEntity commentEntity){
        for(CommentEntity comment : commentEntityList){
            if(comment.id == commentEntity.id){
                commentEntityList.remove(comment);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.thumbnail = (CircleImageView)convertView.findViewById(R.id.user_thumbnail);
            holder.name = (TextView)convertView.findViewById(R.id.user_name);
            holder.commentedAt = (TextView)convertView.findViewById(R.id.commented_at);
            holder.deleteButton = (ImageView)convertView.findViewById(R.id.delete_comment);
            holder.commentContent = (TextView)convertView.findViewById(R.id.comment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CommentEntity comment = getItem(position);

        if(comment.commenter.image != null){
            String thumbnailUrl = ApiService.BASE_URL + comment.commenter.image;
            Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
        }
        if(comment.canDelete) {
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle(context.getText(R.string.prompt_to_delete_comment));
                    int substringEnd;
                    String message = context.getText(R.string.text_delete_comment).toString();
                    if(comment.content == null || comment.content.isEmpty()){
                    }else{
                        if(comment.content.length() > MAX_DELETE_PROMPT){
                            message = message + comment.content.substring(0, MAX_DELETE_PROMPT) + "...";
                        }else{
                            message = context.getText(R.string.text_delete_comment) + comment.content;
                        }
                    }
                    builder.setMessage(message);
                    builder.setPositiveButton(context.getString(R.string.button_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            commentPresenter.deleteComment(comment.itemId, comment.id);
                        }
                    });
                    builder.setNegativeButton(context.getString(R.string.button_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();
                }
            });
        }else{
            holder.deleteButton.setVisibility(View.GONE);
        }
        holder.name.setText(comment.commenter.name);
        holder.commentedAt.setText(ViewUtil.secondsToEasyDateFormat((Activity)context, new Date().getTime() - comment.commentedDate.getTime()) + "前");
        holder.commentContent.setText(comment.content);

        return convertView;
    }

    class ViewHolder{

        CircleImageView thumbnail;
        TextView name;
        TextView commentedAt;
        ImageView deleteButton;
        TextView commentContent;

    }
}