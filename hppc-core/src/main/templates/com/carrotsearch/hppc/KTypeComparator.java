//
// This file (c) 2013 Benjamin Johnson, http://picomancer.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.carrotsearch.hppc;

/**
 * A comparator class for <code>KType</code>s.
 */
public interface KTypeComparator<KType>
{
    public int compare(KType a, KType b);
}
