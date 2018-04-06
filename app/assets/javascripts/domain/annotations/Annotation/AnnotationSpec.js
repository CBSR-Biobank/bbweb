/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 *
 * Jasmine test suite
 */
/* global angular */

import { AnnotationsEntityTestSuiteMixin } from 'test/mixins/AnnotationsEntityTestSuiteMixin';
import ngModule from '../../index'

describe('Annotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, AnnotationsEntityTestSuiteMixin);
      this.injectDependencies('Study',
                              'AnnotationType',
                              'annotationFactory',
                              'Annotation',
                              'Factory');

      this.addCustomMatchers();

      this.createAnnotationType =
        (options = {}) => this.AnnotationType.create(this.Factory.annotationType(options));

      /*
       * Creates a set of annotation type, server annotation and annotation object for each type
       * of annotation.
       */
      this.getAnnotationAndTypeForAllValueTypes = () =>
        this.annotationTypesForAllValueTypes()
        .map((annotationTypeOptions) =>
             this.getAnnotationAndType(annotationTypeOptions)
            );
    });
  });

  it('setValue throws an exception', function() {
    const annotation = new this.Annotation();
    expect(() => {
      annotation.setValue(this.Factory.stringNext());
    }).toThrowError(/derived class must override this method/);
  });

  it('create throws an exception if first param is not an object', function() {
    expect(() => {
      this.Annotation.create(1);
    }).toThrowError(/is not an object/);
  });

  it('create throws an exception annotation type IDs dont match', function() {
    expect(() => {
      this.Annotation.create({ annotationTypeId: this.Factory.stringNext()},
                             this.createAnnotationType());
    }).toThrowError(/annotation type IDs dont match/);
  });

  it('create throws an exception annotation callback does not return a valid object', function() {
    const annotationType = this.createAnnotationType();
    expect(() => {
      this.Annotation.create({ annotationTypeId: annotationType.id },
                             annotationType,
                             () => ({}));
    }).toThrowError(/invalid object to create from/);
  });

  it('isValueValid return valid result', function() {
    const annotationType = this.createAnnotationType();
    const annotation = new this.Annotation({}, annotationType);
    annotation.value = {};
    expect(annotation.isValueValid()).toBeTrue();
  })

});
