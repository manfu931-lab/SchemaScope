package com.schemascope.service;
/*这是第一版的“表名词干提取”。

例如：

owners → owner
users → user
categories → category

为什么要这样做？
因为数据库表经常是复数，而 Java 类通常是单数。

className.contains(tableToken)

这是第一版最核心的匹配规则。

例如：

表 owners → token owner
命中：
Owner
OwnerController
scoreByType(...)

即使都命中了 owner，不同类型的类相关性也不一样：

Owner（ENTITY）更高
OwnerController（CONTROLLER）稍低

所以我们给不同类型不同分数。 */
import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class SchemaChangeComponentMapper {

    public List<ComponentImpactCandidate> mapCandidates(SchemaChange change, JavaProjectScanResult scanResult) {
        List<ComponentImpactCandidate> candidates = new ArrayList<>();

        String tableToken = normalizeTableToken(change.getTableName());

        for (JavaComponent component : scanResult.getComponents()) {
            String className = component.getClassName().toLowerCase();

            if (className.contains(tableToken)) {
                double score = scoreByType(component.getComponentType());
                String reason = "class name matches table token '" + tableToken + "'";
                candidates.add(new ComponentImpactCandidate(component, score, reason));
            }
        }

        candidates.sort(Comparator.comparing(ComponentImpactCandidate::getScore).reversed());
        return candidates;
    }

    private String normalizeTableToken(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "";
        }

        String token = tableName.trim().toLowerCase();

        if (token.endsWith("ies") && token.length() > 3) {
            return token.substring(0, token.length() - 3) + "y";
        }

        if (token.endsWith("s") && token.length() > 1) {
            return token.substring(0, token.length() - 1);
        }

        return token;
    }

    private double scoreByType(JavaComponentType type) {
        return switch (type) {
            case ENTITY -> 0.95;
            case REPOSITORY -> 0.90;
            case SERVICE -> 0.80;
            case CONTROLLER -> 0.75;
            case REST_CONTROLLER -> 0.75;
        };
    }
}