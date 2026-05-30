package com.vanvatcorporation.vanvatsach.read.mobi;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;

import com.vanvatcorporation.vanvatsach.R;

public class MobiReader {

    public static class MobiParser {
        private byte[] fileData;
        int bufferSize = 8192;
        public MobiParser(String filePath) {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(fis.available());
                byte[] buffer = new byte[bufferSize];

                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byteBuffer.put(buffer, 0, bytesRead);
                }

                // Convert ByteBuffer to byte array
                byte[] byteArray = new byte[byteBuffer.position()];
                byteBuffer.flip();
                byteBuffer.get(byteArray);

                System.out.println("File read efficiently!");
                fileData = byteArray;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Extract title from metadata
        public String getTitle() {
            return extractTextFromSection("TITLE");
        }

        // Extract text content (handling decompression)
        public String getTextContent() {
            return decompressText(extractTextFromSection("TEXT"));
        }

        // Extract images
        public List<byte[]> getImages() {
            return extractImages();
        }

        // 🔧 Utility: Extract text based on section headers
        private String extractTextFromSection(String section) {
            // Implement logic to search for section headers and extract relevant data
            return "Example Text Content"; // Placeholder
        }

        // 🔧 Utility: Decompress text (LZ77, Huffman)
        private String decompressText(String compressedText) {
            // Implement custom LZ77 decompression logic here
            return compressedText; // Placeholder
        }

        // 🔧 Utility: Extract images (JPEG, PNG, GIF)
        private List<byte[]> extractImages() {
            List<byte[]> images = new ArrayList<>();

            // Search binary data for image headers (JPEG, PNG, etc.)
            for (int i = 0; i < fileData.length - 2; i++) {
                if ((fileData[i] == (byte) 0xFF && fileData[i + 1] == (byte) 0xD8) ||  // JPEG
                        (fileData[i] == (byte) 0x89 && fileData[i + 1] == (byte) 0x50)) {  // PNG

                    int endIndex = findImageEnd(fileData, i);
                    byte[] imageBytes = Arrays.copyOfRange(fileData, i, endIndex);
                    images.add(imageBytes);
                }
            }
            return images;
        }

        // 🔧 Find the end of an image in binary data
        private int findImageEnd(byte[] data, int startIndex) {
            for (int i = startIndex; i < data.length - 2; i++) {
                if ((data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xD9) ||  // JPEG end
                        (data[i] == (byte) 0x00 && data[i + 1] == (byte) 0x3B)) {  // GIF end
                    return i + 2;
                }
            }
            return data.length;
        }
    }





    public static class MobiAdapter extends RecyclerView.Adapter<MobiAdapter.ViewHolder> {
        private final List<MobiItem> mobiItems;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView mobiImageView;
            public TextView mobiTextView;

            public ViewHolder(View view) {
                super(view);
                mobiImageView = view.findViewById(R.id.mobiImageView);
                mobiTextView = view.findViewById(R.id.mobiTextView);
            }
        }

        public MobiAdapter(List<MobiItem> items) {
            mobiItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cpn_mobi_reader, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MobiItem item = mobiItems.get(position);

            if (item.isImage()) {
                holder.mobiImageView.setVisibility(View.VISIBLE);
                holder.mobiImageView.setImageBitmap(item.getImageBitmap());
                holder.mobiTextView.setVisibility(View.GONE);
            } else {
                holder.mobiTextView.setVisibility(View.VISIBLE);
                holder.mobiTextView.setText(item.getTextContent());
                holder.mobiImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mobiItems.size();
        }
    }
    public static class MobiItem {
        private String textContent;
        private byte[] imageData;

        public MobiItem(String text) {
            textContent = text;
        }

        public MobiItem(byte[] imageBytes) {
            imageData = imageBytes;
        }

        public boolean isImage() {
            return imageData != null;
        }

        public String getTextContent() {
            return textContent;
        }

        public Bitmap getImageBitmap() {
            return imageData != null ? BitmapFactory.decodeByteArray(imageData, 0, imageData.length) : null;
        }
    }

}
