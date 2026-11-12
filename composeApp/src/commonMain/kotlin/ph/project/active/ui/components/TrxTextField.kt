package ph.project.active.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.eye
import projectactive.composeapp.generated.resources.eye_off

@Composable
fun TrxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    fieldTag: String,
    enabled: Boolean,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    errorTag: String? = null,
    errorIconTag: String? = null,
    helper: String? = null,
    helperTag: String? = null,
    password: Boolean = false,
    passwordVisible: Boolean = false,
    passwordToggleTag: String? = null,
    onTogglePassword: (() -> Unit)? = null,
) {
    val colors = LocalTrxColors.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = when {
        errorMessage != null -> colors.dangerFg
        focused -> colors.focusRing
        else -> colors.borderDefault
    }
    val borderWidth = if (focused || errorMessage != null) TrxSpacing.focusWidth else 1.dp
    Column(modifier.fillMaxWidth()) {
        Text(text = label, color = colors.textSecondary, style = TrxTypography.label)
        Row(
            modifier = Modifier
                .padding(top = TrxSpacing.s8)
                .fillMaxWidth()
                .heightIn(min = TrxSpacing.touchTarget)
                .border(borderWidth, borderColor, RoundedCornerShape(TrxSpacing.radiusMd))
                .padding(horizontal = TrxSpacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TrxTypography.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.textPrimary),
                visualTransformation = if (password && !passwordVisible) {
                    BulletMask
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when {
                        password && passwordVisible -> KeyboardType.PasswordVisible
                        password -> KeyboardType.Password
                        else -> keyboardType
                    },
                    autoCorrectEnabled = if (password) false else true,
                ),
                interactionSource = interaction,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = TrxSpacing.touchTarget)
                    .testTag(fieldTag)
                    .semantics {
                        if (password && !passwordVisible) password()
                        if (errorMessage != null) error(errorMessage)
                    },
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, color = colors.textTertiary, style = TrxTypography.body)
                        }
                        inner()
                    }
                },
            )
            if (password && passwordToggleTag != null && onTogglePassword != null) {
                val description = if (passwordVisible) UiCopy.hidePassword() else UiCopy.showPassword()
                Box(
                    modifier = Modifier
                        .size(TrxSpacing.touchTarget)
                        .testTag(passwordToggleTag)
                        .clickable(enabled = enabled, onClick = onTogglePassword)
                        .semantics {
                            role = Role.Button
                            contentDescription = description
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) Res.drawable.eye_off else Res.drawable.eye,
                        ),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        if (errorMessage != null && errorTag != null && errorIconTag != null) {
            Row(
                modifier = Modifier.padding(top = TrxSpacing.s8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = TrxSpacing.s8)
                        .size(16.dp)
                        .testTag(errorIconTag)
                        .drawBehind {
                            val color = colors.dangerFg
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width / 2f, 0f)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(path, color)
                            drawLine(
                                color = colors.textSecondary,
                                start = Offset(size.width / 2f, size.height * 0.35f),
                                end = Offset(size.width / 2f, size.height * 0.62f),
                                strokeWidth = 1.5.dp.toPx(),
                            )
                        },
                )
                Text(
                    text = errorMessage,
                    color = colors.dangerFg,
                    style = TrxTypography.label,
                    modifier = Modifier
                        .testTag(errorTag)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        } else if (helper != null && helperTag != null) {
            Text(
                text = helper,
                color = colors.textTertiary,
                style = TrxTypography.label,
                modifier = Modifier
                    .padding(top = TrxSpacing.s8)
                    .testTag(helperTag),
            )
        }
    }
}

private object BulletMask : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val masked = AnnotatedString("\u2022".repeat(text.text.length))
        return TransformedText(masked, OffsetMapping.Identity)
    }
}
