package uz.pdp.smartinventory.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseCriteria {
    private String search = "";
    private Integer page = 0;
    private Integer size = 10;
}