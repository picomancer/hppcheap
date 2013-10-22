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

import java.util.Arrays;
import com.carrotsearch.hppc.IntHeap;

public class HeapTest
{
    public static boolean nextperm(int p[])
    {
        // find the last ascending pair.
        // reverse the tail, and replace the first member of the found pair with
        //   the next-largest element from the tail.
        int t;

        for(int i=p.length-2;i>=0;i--)
        {
            int a = p[i], b = p[i+1];
            if (a < b)
            {
                // find the smallest element greater than a contained in the tail
                for(int j=i+1,k=p.length-1;j<k;j++,k--)
                {
                    t = p[j];
                    p[j] = p[k];
                    p[k] = t;
                }

                int h = -Arrays.binarySearch(p, i+1, p.length, a) - 1;
                t = p[h];
                p[h] = a;
                p[i] = t;
                return true;
            }
        }

        for(int j=0,k=p.length-1;j<k;j++,k--)
        {
            t = p[j];
            p[j] = p[k];
            p[k] = t;
        }

        return false;
    }

    public static void perm_test()
    {
        int p[] = new int[10];
        for(int i=0;i<p.length;i++)
            p[i] = i;

        int n = 0;
        do
        {
            IntHeap h = new IntHeap();
            for(int i=0;i<p.length;i++)
                h.add(p[i]);
            for(int i=0;i<p.length;i++)
            {
                int j = h.pop();
                if (j != i)
                {
                    System.out.print("Heap doesn't work on input ");
                    for(int k=0;k<p.length;k++)
                    {
                        System.out.print(p[k]+" ");
                    }
                    System.out.println("");
                }
            }
            n++;
        } while(nextperm(p));

        return;
    }

    public static void main(String arg[])
        throws Exception
    {
        perm_test();
        return;
    }
}
