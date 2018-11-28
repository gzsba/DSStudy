import com.github.abel533.entity.Example;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description PACKAGE_NAME
 * @date 2018-10-25
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testGetById(){
        User user = userMapper.selectByPrimaryKey(35);
        System.out.println(user);
    }

    @Test
    public void testGetAll(){
        List<User> users = userMapper.select(null);
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testGetByExample(){
        //组装查询条件
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        //组装性别条件
        criteria.andEqualTo("sex","0");

        List<User> users = userMapper.selectByExample(example);
        for (User user : users) {
            System.out.println(user);
        }
    }
}
