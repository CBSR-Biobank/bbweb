/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../../../test/behaviours/labelServiceSharedBehaviour';

describe('annotationValueTypeLabelService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('annotationValueTypeLabelService',
                              'AnnotationValueType');
    });
  });

  describe('shared behaviour', function() {
    const context = {};
    beforeEach(function() {
      context.labels = _.values(this.AnnotationValueType);
      context.toLabelFunc =
        this.annotationValueTypeLabelService.valueTypeToLabelFunc.bind(this.annotationValueTypeLabelService);
      context.expectedLabels = [];

      Object.values(this.AnnotationValueType).forEach(valueType => {
        if (valueType === this.AnnotationValueType.DATE_TIME) {
          context.expectedLabels[valueType] = 'Date and time';
        } else {
          context.expectedLabels[valueType] = this.capitalizeFirstLetter(valueType);
        }
      });
    });
    sharedBehaviour(context);
  });

  it('has valid enumeration values', function() {
    var labels = this.annotationValueTypeLabelService.getLabels(),
        labelIds = _.map(labels, 'id');
    expect(labels.length).toBe(_.keys(this.AnnotationValueType).length);
    Object.values(this.AnnotationValueType).forEach(valueType => {
      expect(labelIds).toContain(valueType);
    });
    _.map(labels, 'labelFunc').forEach(labelFunc => {
      expect(labelFunc).toBeFunction();
    });
  });

});
