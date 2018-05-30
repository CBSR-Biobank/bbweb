/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

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
   * @param {object} context.$httBackend Angular's $httpBackend.
   *
   * @param {string} context.addUrl the URL on the server to add the annotation.
   *
   * @param {string} context.deleteUrl the URL on the server to remove the annotation.
   *
   * @param {object} context.response The response from the server.
   */
  function entityWithAnnotationsSharedBehaviour(context) {

    describe('(shared)', function () {

      beforeEach(function() {
        this.injectDependencies('$rootScope', 'Factory');
      });

      it('can add annotation', function () {
        this.updateEntity(context.entity,
                          context.updateFuncName,
                          context.annotation,
                          context.addUrl,
                          _.pick(context.annotation.getServerAnnotation(),
                                 [
                                   'annotationTypeId',
                                   'stringValue',
                                   'numberValue',
                                   'selectedValues'
                                 ]),
                          context.response,
                          expectEntity,
                          failTest);
      });

      it('can remove an annotation', function () {
        context.$httpBackend.whenDELETE(context.removeUrl).respond(201, {
          status: 'success',
          data:   context.response
        });
        context.entity[context.removeFuncName](context.annotation)
          .then(expectEntity)
          .catch(failTest);
        context.$httpBackend.flush();
      });

      it('fails when removing an invalid annotation', function () {
        var annotation = Object.assign({}, context.annotation, { annotationTypeId: this.Factory.stringNext() });
        context.entity[context.removeFuncName](annotation)
          .catch(function (err) {
            expect(err).toStartWith('annotation with annotation type ID not present:');
          });
        this.$rootScope.$digest();
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

  return entityWithAnnotationsSharedBehaviour;
});
