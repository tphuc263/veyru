package share_app.tphucshareapp.dto.request.search;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;
    private String type; // "users", "photos", "all"
    private int page = 0;
    private int size = 20;
}
