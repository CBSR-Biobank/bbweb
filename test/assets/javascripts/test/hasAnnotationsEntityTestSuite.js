/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  hasAnnotationsEntityTestSuite.$inject = [
    'AnnotationType',
    'AnnotationValueType',
    'TextAnnotation',
    'DateTimeAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'MultipleSelectAnnotation',
    'jsonEntities'
  ];

  /**
   * A mixin for test suites for domain entities.
   */
  function hasAnnotationsEntityTestSuite(AnnotationType,
                                         AnnotationValueType,
                                         TextAnnotation,
                                         DateTimeAnnotation,
                                         NumberAnnotation,
                                         SingleSelectAnnotation,
                                         MultipleSelectAnnotation,
                                         jsonEntities) {
    var mixin = {
      jsonAnnotationData: jsonAnnotationData,
      validateAnnotationClass: validateAnnotationClass,
      annotationSetSharedSpec: annotationSetSharedSpec
    };

    return mixin;

    //--

    function jsonAnnotationData(serverStudy) {
      var annotationTypes = jsonEntities.allAnnotationTypes();

      return _.map(annotationTypes, function (annotationType) {
        var value = jsonEntities.valueForAnnotation(annotationType);
        var annotation = jsonEntities.annotation(value, annotationType);

        return {
          annotationType: annotationType,
          annotation:     annotation
        };
      });
    }

    /**
     * @param {AnnotationType} annotationType the AnnotationType this annotion is based on.
     *
     * @param {Annotation} the annotation.
     */
    function validateAnnotationClass(annotationType, annotation) {
      switch (annotationType.valueType) {
      case AnnotationValueType.TEXT():
        expect(annotation).toEqual(jasmine.any(TextAnnotation));
        break;
      case AnnotationValueType.DATE_TIME():
        expect(annotation).toEqual(jasmine.any(DateTimeAnnotation));
        break;
      case AnnotationValueType.NUMBER():
        expect(annotation).toEqual(jasmine.any(NumberAnnotation));
        break;
      case AnnotationValueType.SELECT():
        if (annotationType.isSingleSelect()) {
          expect(annotation).toEqual(jasmine.any(SingleSelectAnnotation));
        } else {
          expect(annotation).toEqual(jasmine.any(MultipleSelectAnnotation));
        }
        break;

      default:
        fail('invalid annotation value type: ' + annotationType.valueType);
      }
    }
    /**
     * @param {object} context.entityType the parent entity.
     *
     * @param {object} context.entity the parent entity.
     *
     * @param {string} context.updateFuncName the name of the function on the entity to add the annotation.
     *
     * @param {string} context.removeFuncName the name of the function on the entity to add the annotation.
     *
     * @param {object} context.annotation the annotation to add.
     *
     * @param {string} context.addUrl the URL on the server to add the annotation.
     *
     * @param {string} context.deleteUrl the URL on the server to remove the annotation.
     *
     * @param {object} context.response The response from the server.
     */
    function annotationSetSharedSpec(context) {

      describe('(shared)', function () {

        it('can add annotation', function () {
          this.updateEntity(context.entity,
                            context.updateFuncName,
                            context.annotation,
                            context.addUrl,
                            _.pick(context.annotation, 'stringValue', 'numberValue', 'selectedValues'),
                            context.response,
                            expectEntity,
                            failTest);
        });

        it('can remove an annotation', function () {
          var self = this;

          self.httpBackend.whenDELETE(context.removeUrl).respond(201, { status: 'success', data: true });
          context.entity[context.removeFuncName](context.annotation)
            .then(self.expectParticipant)
            .catch(self.failTest);
          self.httpBackend.flush();
        });

      });

      // used by promise tests
      function expectEntity(entity) {
        expect(entity).toEqual(jasmine.any(context.entityType));
      }

      // used by promise tests
      function failTest(error) {
        expect(error).toBeUndefined();
      }
    }

  }

  return hasAnnotationsEntityTestSuite;

});
