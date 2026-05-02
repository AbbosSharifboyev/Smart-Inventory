package uz.pdp.smartinventory.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.pdp.smartinventory.model.domain.Permission;
import uz.pdp.smartinventory.repository.PermissionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PermissionRepository repository;

    @Override
    public void run(String... args) throws Exception {
        // Faqat bazada permissionlar bo'lmasa ishga tushadi
        if (repository.count() == 0){
            List<String> perms = List.of("PRODUCT_READ", "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE",
                    "CATEGORY_READ", "CATEGORY_CREATE", "CATEGORY_UPDATE", "CATEGORY_DELETE",
                    "USER_MANAGE"
            );
            for (String name : perms) {
                Permission p = new Permission();
                p.setName(name);
                p.setDescription(name + " huquqi");
                repository.save(p);
            }
            System.out.println(">>> Permissionlar bazaga muvaffaqiyatli saqlandi.");
        }


    }
}
