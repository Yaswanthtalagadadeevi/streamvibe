import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StreamAdapter extends RecyclerView.Adapter<StreamAdapter.StreamViewHolder> {

    private final List<Stream> streamList;

    public StreamAdapter(List<Stream> streamList) {
        this.streamList = streamList;
    }

    @NonNull
    @Override
    public StreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new StreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StreamViewHolder holder, int position) {
        Stream stream = streamList.get(position);
        holder.hostName.setText("Host: " + stream.getHostName());
        holder.streamTitle.setText("Title: " + stream.getStreamTitle());
        // Optionally, you can set other details here as well
    }

    @Override
    public int getItemCount() {
        return streamList.size();
    }

    static class StreamViewHolder extends RecyclerView.ViewHolder {
        TextView hostName;
        TextView streamTitle;

        public StreamViewHolder(@NonNull View itemView) {
            super(itemView);
            hostName = itemView.findViewById(android.R.id.text1);
            streamTitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
