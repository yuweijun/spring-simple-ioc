 

package org.springframework.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Juergen Hoeller
 * @since 11.11.2003
 */
public class IndexedTestBean {

	private TestBean[] array;

	private List list;

	private Set set;

	private Map map;

	public IndexedTestBean() {
		TestBean tb0 = new TestBean("name0", 0);
		TestBean tb1 = new TestBean("name1", 0);
		TestBean tb2 = new TestBean("name2", 0);
		TestBean tb3 = new TestBean("name3", 0);
		TestBean tb4 = new TestBean("name4", 0);
		TestBean tb5 = new TestBean("name5", 0);
		TestBean tb6 = new TestBean("name6", 0);
		TestBean tb7 = new TestBean("name7", 0);
		this.array = new TestBean[] {tb0, tb1};
		this.list = new ArrayList();
		this.list.add(tb2);
		this.list.add(tb3);
		this.set = new TreeSet();
		this.set.add(tb6);
		this.set.add(tb7);
		this.map = new HashMap();
		this.map.put("key1", tb4);
		this.map.put("key2", tb5);
	}

	public TestBean[] getArray() {
		return array;
	}

	public void setArray(TestBean[] array) {
		this.array = array;
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set set) {
		this.set = set;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

}
