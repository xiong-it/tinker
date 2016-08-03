/*
 * Copyright (C) 2016 Tencent WeChat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tinker.build.dexpatcher.algorithms.diff;

import com.tencent.tinker.android.dex.AnnotationsDirectory;
import com.tencent.tinker.android.dex.Dex;
import com.tencent.tinker.android.dex.TableOfContents;
import com.tencent.tinker.android.dex.io.DexDataBuffer;
import com.tencent.tinker.android.dx.util.IndexMap;

/**
 * Created by tangyinsheng on 2016/6/30.
 */
public class AnnotationsDirectorySectionDiffAlgorithm extends DexSectionDiffAlgorithm<AnnotationsDirectory> {
    public AnnotationsDirectorySectionDiffAlgorithm(Dex oldDex, Dex newDex, IndexMap oldToNewIndexMap, IndexMap oldToPatchedIndexMap, IndexMap selfIndexMapForSkip) {
        super(oldDex, newDex, oldToNewIndexMap, oldToPatchedIndexMap, selfIndexMapForSkip);
    }

    @Override
    protected TableOfContents.Section getTocSection(Dex dex) {
        return dex.getTableOfContents().annotationsDirectories;
    }

    @Override
    protected AnnotationsDirectory nextItem(DexDataBuffer section) {
        return section.readAnnotationsDirectory();
    }

    @Override
    protected int getItemSize(AnnotationsDirectory item) {
        return item.byteCountInDex();
    }

    @Override
    protected AnnotationsDirectory adjustItem(IndexMap indexMap, AnnotationsDirectory item) {
        return indexMap.adjust(item);
    }

    @Override
    protected void updateIndexOrOffset(IndexMap indexMap, int oldIndex, int oldOffset, int newIndex, int newOffset) {
        if (oldOffset != newOffset) {
            indexMap.mapAnnotationsDirectoryOffset(oldOffset, newOffset);
        }
    }
}
