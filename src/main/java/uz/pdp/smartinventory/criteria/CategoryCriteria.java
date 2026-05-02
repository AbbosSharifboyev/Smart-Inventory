package uz.pdp.smartinventory.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.QAbstractAuditable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCriteria extends BaseCriteria{
    private String name;
}
