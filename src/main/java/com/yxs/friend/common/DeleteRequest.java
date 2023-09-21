package com.yxs.friend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: yxs
 * @create: 2023-08-07 21:03
 **/
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 8997857401210299898L;

   private long id ;
}
