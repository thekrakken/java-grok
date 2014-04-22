/*******************************************************************************
 * Copyright 2014 Anthony Corbacho and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nflabs.grok;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Leon the professional of {@code Grok}
 *
 * @author anthonycorbacho
 * @since 0.0.2
 */
public class Garbage {

  private List<String> _remove;
  private Map<String, Object> _rename;

  /**
   * Create a new {@code Garbage} object
   */
  public Garbage() {

    _remove = new ArrayList<String>();
    _rename = new TreeMap<String, Object>();
    _remove.add("UNWANTED");

  }

  /**
   * Set a map of matched field to re name
   *
   * @param key : Original field name
   * @param value : New field name to apply
   */
  public void addToRename(String key, Object value) {
    if (key == null || value == null)
      return;
    if (!key.isEmpty() && !value.toString().isEmpty())
      _rename.put(key, value);
  }

  /**
   * Set a field name to be remove from the final matched map
   *
   * @param item : Name of the field to remove
   */
  public void addToRemove(String item) {
    if (item == null)
      return;
    if (!item.isEmpty())
      _remove.add(item);
  }

  /**
   * Set a list of field name to be remove from the final matched map
   *
   * @param lst
   */
  public void addFromListRemove(List<String> lst) {
    if (lst == null)
      return;
    if (!lst.isEmpty())
      _remove.addAll(lst);
  }

  /**
   * Remove from the map the unwilling items
   *
   * @param map to clean
   * @return nb of deleted item
   */
  public int remove(Map<String, Object> map) {
    int item = 0;

    if (map == null)
      return -1;
    if (map.isEmpty())
      return -1;
    for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Object> entry = it.next();
      for (int i = 0; i < _remove.size(); i++)
        if (entry.getKey().equals(_remove.get(i))) {
          it.remove();
          item++;
        }
    }
    return item;
  }

  /**
   * Rename the item from the map
   *
   * @param map
   * @return nb of renamed items
   */
  public int rename(Map<String, Object> map) {
    int item = 0;

    if (map == null)
      return -1;
    if (map.isEmpty() || _rename.isEmpty())
      return -1;

    for (Iterator<Map.Entry<String, Object>> it = _rename.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Object> entry = it.next();
      if (map.containsKey(entry.getKey())) {
        Object obj = map.remove(entry.getKey());
        map.put(entry.getValue().toString(), obj);
        item++;
      }
    }
    return item;
  }

}
