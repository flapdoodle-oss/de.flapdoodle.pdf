package de.flapdoodle.pdf.elements;

import com.google.common.base.Preconditions;
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

	protected abstract List<PdfPCellFactory> cells();

	@Value.Check
	protected void check() {
		Preconditions.checkArgument(widthPercentage() > 0, "widthPercentage must be > 0");
		int numberOfColumns = numberOfColumns(columns());
		int invisibleCells = cells().size() % numberOfColumns;
		
		Preconditions.checkArgument(invisibleCells == 0,
			"%s cells does not match %s columns", cells().size(), numberOfColumns
		);
	}

	@Override
	@Value.Auxiliary
	public PdfPTable create() {
		PdfPTable table = createTable(columns());
		table.setWidthPercentage(widthPercentage());
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

		static Columns relativeWeights(float ... weights) {
			return new RelativeWeights(FloatArray.from(weights));
		}
	}

	public static ImmutableTableElement.Builder builder() {
		return ImmutableTableElement.builder();
	}
}
