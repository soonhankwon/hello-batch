package dev.hellobatch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ClassInformation {

    private String name;
    private int size;

    public ClassInformation(String name, int size) {
        this.name = name;
        this.size = size;
    }
}
