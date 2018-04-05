/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../../../base/services/LabelService';
import _ from 'lodash';

/**
 * An AngularJS service that converts an {@link domain.annotations.AnnotationType#valueType AnnotationType.valueType}
 * to a *translated string* that can be displayed to the user.
 *
 * @memberOf admin.common.services
 */
class AnnotationValueTypeLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {AngularJS_Service} gettextCatalog
   *
   * @param {domain.AnnotationValueType} AnnotationValueType - value type enumeration.
   *
   * @param {base.services.LabelService} labelService
   */
  constructor(BbwebError, gettextCatalog, AnnotationValueType) {
    'ngInject';
    super(BbwebError,
          [
            { id: AnnotationValueType.TEXT, label: () => gettextCatalog.getString('Text') },
            { id: AnnotationValueType.NUMBER, label: () => gettextCatalog.getString('Number') },
            { id: AnnotationValueType.DATE_TIME, label: () => gettextCatalog.getString('Date and time') },
            { id: AnnotationValueType.SELECT, label: () => gettextCatalog.getString('Select') }
          ]);
    Object.assign(this, { AnnotationValueType, gettextCatalog });
  }

  /**
   * Returns a function that should be called to display the label for a {@link domain.AnnotationValueType
   * AnnotationValueType}.
   *
   * @param {domain.AnnotationValueType} valueType - the value type to get a function for.
   *
   * @param {boolean} [isSingleSelect] - Set to `TRUE` to return the function for a `SINGLE SELECT` {@link
   * domain.annotations.AnnotationType AnnotationType}. Used only when `valueType` is {@link domain.annotations.AnnotationValueType
   * SELECT}.
   *
   * @return {function} a function that returns a label that can be displayed to the user.
   */
  valueTypeToLabelFunc(valueType, isSingleSelect) {
    if (valueType === this.AnnotationValueType.SELECT && !_.isUndefined(isSingleSelect)) {
      if (isSingleSelect) {
        return () => this.gettextCatalog.getString('Single Select');
      } else {
        return () => this.gettextCatalog.getString('Multiple Select');
      }
    }

    return this.getLabel(valueType);
  }

  /**
   * Returns the functions for all {@link domain.AnnotationValueType AnnotationValueTypes}.
   *
   * @return {Array<admin.common.services.AnnotationValueTypeLabelService.LabelInfo>}
   */
  getLabels() {
    return Object.values(this.AnnotationValueType).map(valueType => ({
      id:        valueType,
      labelFunc: this.valueTypeToLabelFunc(valueType)
    }));
  }

}

/**
 * Object that contains the function that returns the label for the currently selected language for an
 * {@link domain.AnnotationValueType AnnotationValueType}.
 *
 * @typedef admin.common.services.AnnotationValueTypeLabelService.LabelInfo
 *
 * @type object
 *
 * @property {domain.AnnotationValueType} valueType - the value type for this function.
 *
 * @property {function} labelFunc - the function that returns the label for the value type for currently
 * selected language.
 */

export default ngModule => ngModule.service('annotationValueTypeLabelService',
                                           AnnotationValueTypeLabelService)
