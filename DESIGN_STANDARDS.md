# Design Standards and UI Architecture

## Overview
This document defines the design standards, UI patterns, and visual architecture for the SendaSnap Android application. All UI components should follow these guidelines to ensure consistency and maintainability.

## Page Structure

### Standard Activity Layout
Every new activity page should follow this structure:

```xml
<LinearLayout or CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/color_background"
    android:elevation="12dp"
    android:fitsSystemWindows="true">

    <!-- App Bar -->
    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/appBarLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/transparent"
        app:elevation="4dp">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@android:color/transparent"
            app:title="Page Title"
            app:titleTextColor="@color/text_primary"
            app:titleTextAppearance="@style/TextAppearance.Material3.ActionBar.Title"
            app:navigationIcon="@drawable/ic_arrow_back"
            app:navigationIconTint="@color/text_primary"/>

    </com.google.android.material.appbar.AppBarLayout>

    <!-- Content -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true"
        android:scrollbars="none"
        android:overScrollMode="never">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="@dimen/dimen_16"
            android:paddingBottom="32dp">

            <!-- Page Content Here -->

        </LinearLayout>

    </androidx.core.widget.NestedScrollView>

</LinearLayout>
```

### Alternative: CoordinatorLayout with Gradient Background
For pages with gradient backgrounds (like Vehicle Details):

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:elevation="12dp"
    android:fitsSystemWindows="true"
    android:background="@drawable/professional_gradient">

    <!-- App Bar (same as above) -->

    <!-- Content with scrolling behavior -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <!-- Content -->

    </androidx.core.widget.NestedScrollView>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

## Toolbar Design

### Standard Toolbar Configuration
- **Height**: `?attr/actionBarSize` (56dp)
- **Background**: Transparent (`@android:color/transparent`)
- **Elevation**: 4dp on AppBarLayout
- **Navigation Icon**: `@drawable/ic_arrow_back`
- **Navigation Icon Tint**: `@color/text_primary` or `@color/black`
- **Title Text Appearance**: `@style/TextAppearance.Material3.ActionBar.Title`
- **Title Text Color**: `@color/text_primary`

### Toolbar Setup in Activity
```java
private void setupToolbar() {
    setSupportActionBar(binding.toolbar);
    if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    binding.toolbar.setNavigationOnClickListener(v -> finish());
}
```

## Text Sizes

### Standard Text Size Hierarchy
Use these standardized text sizes from `dimens.xml`:

- **Large**: `@dimen/text_size_large` (16sp) - Headers, important titles
- **Medium**: `@dimen/text_size_medium` (14sp) - Section headers, card titles
- **Small**: `@dimen/text_size_small` (12sp) - Body text, labels, most content
- **XSmall**: `@dimen/text_size_xsmall` (10sp) - Secondary text, hints
- **XXSmall**: `@dimen/text_size_xxsmall` (9sp) - Fine print, timestamps

### Text Size Usage Guidelines
- **Section Headers**: `text_size_medium` with `textStyle="bold"`
- **Card Titles**: `text_size_medium` with `textStyle="bold"`
- **Body Text**: `text_size_small`
- **Labels**: `text_size_small`
- **Secondary Text**: `text_size_small` with `textColor="@color/text_secondary"`
- **Hints**: `text_size_xsmall` or `text_size_small`

## Margins and Padding

### Standard Spacing Values
Use these dimension resources from `dimens.xml`:

- **4dp**: `@dimen/dimen_4` - Tight spacing, icon padding
- **8dp**: `@dimen/dimen_8` - Small spacing, chip margins
- **12dp**: `@dimen/dimen_12` - Medium spacing, card padding
- **16dp**: `@dimen/dimen_16` - Standard spacing, section padding, horizontal padding
- **20dp**: `@dimen/dimen_20` - Large spacing
- **24dp**: `@dimen/dimen_24` - Extra large spacing
- **32dp**: `32dp` - Bottom padding for scrollable content

### Spacing Guidelines

#### Page Level
- **Horizontal Padding**: `@dimen/dimen_16` (16dp)
- **Bottom Padding**: `32dp` (for scrollable content)

#### Section Level
- **Section Top Margin**: `@dimen/dimen_16` (16dp)
- **Section Padding**: `@dimen/dimen_16` (16dp) or `@dimen/dimen_12` (12dp)

#### Component Level
- **Card Margin Top**: `@dimen/dimen_16` (16dp)
- **Card Padding**: `@dimen/dimen_16` (16dp)
- **Label Bottom Margin**: `@dimen/dimen_8` (8dp) or `4dp`
- **Item Bottom Margin**: `4dp` or `@dimen/dimen_4`
- **Chip Spacing**: `8dp` horizontal, `8dp` vertical

#### Text Spacing
- **Label to Value**: `4dp` margin bottom on label
- **Section Title Bottom**: `8dp` margin bottom
- **Paragraph Spacing**: `8dp` or `12dp`

## Color System

### Text Colors
- **Primary Text**: `@color/text_black` or `@color/text_primary` - Main content
- **Secondary Text**: `@color/text_secondary` - Labels, hints, less important text
- **Muted Text**: `@color/text_muted` - Very secondary information

### Background Colors
- **Page Background**: `@color/color_background` (#FDFDFD)
- **Card Background**: `@color/white` or `@color/primary_light`
- **Gradient Background**: `@drawable/professional_gradient`

### Status Colors
- **Success**: `@color/success` with `@color/success_light` background
- **Error**: `@color/error` with `@color/error_light` background
- **Warning**: `@color/warning` with `@color/warning_light` background
- **Primary**: `@color/primary` with `@color/primary_light` background

## Typography

### Font Families
- **Bold Headers**: `@font/montserrat_bold`
- **Semi-Bold Text**: `@font/montserrat_semi_bold` (most common)
- **Regular Text**: Default system font or `@font/montserrat_semi_bold`

### Font Usage
- **Section Headers**: `montserrat_semi_bold` with `textStyle="bold"`
- **Card Titles**: `montserrat_semi_bold`
- **Body Text**: `montserrat_semi_bold`
- **Labels**: `montserrat_semi_bold`
- **Priority/Status Chips**: `montserrat_bold` for emphasis

### Text Properties
- **Include Font Padding**: `android:includeFontPadding="false"` for better spacing
- **Letter Spacing**: `0.01` for buttons (optional)

## Card Design

### Standard Card Configuration
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/dimen_16"
    app:cardCornerRadius="12dp"
    app:cardElevation="0dp"
    app:cardBackgroundColor="@color/primary_light"
    app:strokeColor="@color/primary_light"
    app:strokeWidth="0dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/dimen_16">

        <!-- Card Content -->

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

### Card Variants
- **Default**: `@color/primary_light` background, no stroke
- **With Border**: Add `app:strokeColor` and `app:strokeWidth="1dp"`
- **Status Cards**: Use status colors (success_light, error_light, warning_light)

## Form Input Design

### TextInputLayout Configuration
```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Label"
    app:boxStrokeColor="@color/primary"
    app:boxStrokeWidthFocused="2dp"
    app:hintTextColor="@color/text_secondary"
    app:startIconTint="@color/black"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/editText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textCapSentences"
        android:textSize="@dimen/text_size_small"
        android:fontFamily="@font/montserrat_semi_bold"
        android:textColor="@color/text_black"/>

</com.google.android.material.textfield.TextInputLayout>
```

### Input Spacing
- **Top Margin**: `@dimen/dimen_4` or `@dimen/dimen_8` between inputs
- **Section Margin**: `@dimen/dimen_16` between input sections

## Chip Design

### Standard Chip Configuration
```xml
<com.google.android.material.chip.Chip
    style="@style/Widget.Material3.Chip.Filter"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Label"
    android:textSize="@dimen/text_size_small"
    android:fontFamily="@font/montserrat_semi_bold"
    android:textColor="@color/black"
    app:chipBackgroundColor="@color/white"
    app:chipCornerRadius="24dp"
    app:chipStrokeColor="@color/warning_dark"
    app:chipStrokeWidth="2dp"/>
```

### Chip Group Configuration
```xml
<com.google.android.material.chip.ChipGroup
    android:id="@+id/chipGroup"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:chipSpacingHorizontal="8dp"
    app:chipSpacingVertical="8dp"
    app:singleSelection="true">
```

## Button Design

### Primary Button
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/buttonPrimary"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Button Text"
    android:textAppearance="@style/TextAppearance.Montserrat.LabelMedium"
    android:textStyle="bold"
    android:textColor="@android:color/white"
    android:minWidth="160dp"
    app:backgroundTint="@color/primary"
    app:cornerRadius="4dp"
    app:icon="@drawable/ic_add"
    app:iconGravity="textStart"
    app:iconPadding="8dp"
    app:iconTint="@android:color/white"
    app:rippleColor="@color/primary_dark"/>
```

### Text Button
```xml
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button.TextButton"
    android:layout_width="wrap_content"
    android:layout_height="56dp"
    android:text="Cancel"
    android:textAppearance="@style/TextAppearance.Montserrat.LabelLarge"
    android:textColor="@color/text_secondary"
    android:textStyle="bold"
    android:minWidth="140dp"
    app:cornerRadius="16dp"
    app:rippleColor="@color/primary_light"/>
```

### Button Heights
- **Standard**: `56dp` (wrap_content with minHeight)
- **Icon Buttons**: `100dp x 100dp` for square icon buttons

## Section Headers

### Standard Section Header
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    android:text="Section Title"
    android:textColor="@color/text_black"
    android:textSize="@dimen/text_size_medium"
    android:textStyle="bold"
    android:fontFamily="@font/montserrat_semi_bold"/>
```

## Data Display Patterns

### Key-Value Pairs (Horizontal)
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:layout_marginBottom="4dp">

    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Label:"
        android:textColor="@color/text_black"
        android:fontFamily="@font/montserrat_semi_bold"
        android:textSize="@dimen/text_size_small"/>

    <TextView
        android:id="@+id/txtValue"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Value"
        android:textColor="@color/text_primary"
        android:fontFamily="@font/montserrat_semi_bold"
        android:textSize="@dimen/text_size_small"/>

</LinearLayout>
```

### Key-Value Pairs (Vertical)
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="4dp"
        android:fontFamily="@font/montserrat_semi_bold"
        android:text="Label"
        android:textColor="@color/text_secondary"
        android:textSize="@dimen/text_size_small"
        android:includeFontPadding="false"/>

    <TextView
        android:id="@+id/txtValue"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:fontFamily="@font/montserrat_semi_bold"
        android:textColor="@color/text_black"
        android:textSize="@dimen/text_size_small"
        android:includeFontPadding="false"/>

</LinearLayout>
```

## RecyclerView Configuration

### Standard RecyclerView
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:nestedScrollingEnabled="false"
    android:visibility="gone"/>
```

## Empty States

### Standard Empty State
```xml
<TextView
    android:id="@+id/textEmpty"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/dimen_16"
    android:padding="@dimen/dimen_16"
    android:gravity="center"
    android:fontFamily="@font/montserrat_semi_bold"
    android:text="No items found"
    android:textColor="@color/text_secondary"
    android:textSize="@dimen/text_size_small"
    android:includeFontPadding="false"/>
```

## Activity Setup Pattern

### Standard Activity onCreate
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    
    binding = ActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    
    MyApplication.applyWindowInsets(binding.getRoot());
    
    initHelpers();
    setupToolbar();
    setupClickListeners();
    loadData();
}

private void setupToolbar() {
    setSupportActionBar(binding.toolbar);
    if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    binding.toolbar.setNavigationOnClickListener(v -> finish());
}
```

## Design Checklist

When creating a new page, ensure:
- [ ] Toolbar follows standard configuration
- [ ] Text sizes use dimension resources
- [ ] Margins and padding use dimension resources
- [ ] Colors use color resources (no hardcoded colors)
- [ ] Font families are consistent
- [ ] Cards use standard MaterialCardView configuration
- [ ] Form inputs use TextInputLayout with proper styling
- [ ] Buttons follow standard button patterns
- [ ] Section headers are consistent
- [ ] Spacing is consistent throughout
- [ ] Background colors match design system
- [ ] Empty states are included where needed

## Common Patterns

### Information Display Card
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/dimen_16"
    app:cardCornerRadius="12dp"
    app:cardElevation="0dp"
    app:cardBackgroundColor="@color/primary_light">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/dimen_16">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:text="Section Title"
            android:textColor="@color/text_black"
            android:textSize="@dimen/text_size_medium"
            android:textStyle="bold"
            android:fontFamily="@font/montserrat_semi_bold"/>

        <!-- Content -->

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

### Form Section
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:layout_marginTop="@dimen/dimen_16">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Section Title"
        android:fontFamily="@font/montserrat_semi_bold"
        android:textColor="@color/text_black"
        android:textStyle="bold"/>

    <!-- Form Inputs -->

</LinearLayout>
```

## Examples

### Vehicle Details Pattern
- CoordinatorLayout with gradient background
- Multiple information cards with 16dp padding
- Key-value pairs in horizontal layout
- Section headers with 8dp bottom margin
- Card spacing of 16dp

### Schedule Detail Pattern
- LinearLayout with standard background
- MaterialCardView for each section
- 12dp card corner radius
- 16dp horizontal padding
- Vertical spacing of 16dp between cards

### Add Schedule Pattern
- NestedScrollView for scrollable content
- TextInputLayout for form inputs
- Chip groups for selections
- Action buttons at bottom
- 32dp bottom padding for scrollable content

