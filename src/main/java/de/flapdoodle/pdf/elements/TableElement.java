/*
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.pdf.elements;

import de.flapdoodle.commons.checks.Preconditions;
import com.lowagie.text.pdf.PdfPTable;
import de.flapdoodle.pdf.types.FloatArray;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class TableElement implements ElementSupplier<PdfPTable> {

	protected abstract Columns columns();

	@Value.Default
	protected float widthPercentage() {
		return 100.0f;
	}

	@Value.Default
	protected float spacingBefore() {
		return 0.0f;
	}

	@Value.Default
	protected float spacingAfter() {
		return 0.0f;
	}

	@Value.Default
	protected boolean splitRows() {
		return true;
	}

	@Value.Default
	protected boolean splitLate() {
		return true;
	}

	@Value.Default
	protected boolean keepTogether() {
		return false;
	}

	public abstract List<PdfPCellFactory> cells();

	@Value.Lazy
	public int numberOfColumns() {
		return numberOfColumns(columns());
	}

	@Value.Auxiliary
	public ImmutableTableElement copy() {
		return ImmutableTableElement.copyOf(this);
	}

	@Value.Check
	protected void check() {
		Preconditions.checkArgument(widthPercentage() > 0, "widthPercentage must be > 0");

		int numberOfColumns = numberOfColumns(columns());
		int numberOfCells = cells().stream()
			.map(cell -> cell.rowSpan() * cell.colSpan())
			.mapToInt(i -> i)
			.sum();

		int invisibleCells = numberOfCells % numberOfColumns;

		Preconditions.checkArgument(invisibleCells == 0,
			"%s cells does not match %s columns", numberOfCells, numberOfColumns
		);
	}

	@Override
	@Value.Auxiliary
	public PdfPTable create() {
		PdfPTable table = createTable(columns());
		table.setWidthPercentage(widthPercentage());
		table.setSpacingBefore(spacingBefore());
		table.setSpacingAfter(spacingAfter());
		table.setSplitRows(splitRows());
		table.setSplitLate(splitLate());
		table.setKeepTogether(keepTogether());
		cells().forEach(f -> {
			table.addCell(f.create(table.getDefaultCell()));
		});
		return table;
	}

	private static PdfPTable createTable(Columns columns) {
		if (columns instanceof Columns.Count) {
			return new PdfPTable(((Columns.Count) columns).count());
		}
		if (columns instanceof Columns.RelativeWeights) {
			return new PdfPTable(((Columns.RelativeWeights) columns).weights().array());
		}
		throw new IllegalArgumentException("columns must be of type Columns.Count, Columns.RelativeWeights");
	}

	private static int numberOfColumns(Columns columns) {
		if (columns instanceof Columns.Count) {
			return ((Columns.Count) columns).count();
		}
		if (columns instanceof Columns.RelativeWeights) {
			return ((Columns.RelativeWeights) columns).weights().length();
		}
		throw new IllegalArgumentException("columns must be of type Columns.Count, Columns.RelativeWeights");
	}

	public sealed interface Columns {
		record Count(int count) implements Columns {
		}

		record RelativeWeights(FloatArray weights) implements Columns {
		}

		static Columns count(int count) {
			return new Count(count);
		}

		static Columns relativeWeights(float... weights) {
			return new RelativeWeights(FloatArray.from(weights));
		}
	}

	public static ImmutableTableElement.Builder builder() {
		return ImmutableTableElement.builder();
	}
}
