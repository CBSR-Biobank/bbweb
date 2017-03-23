/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
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
  function entityWithAnnotationsSharedSpec(context) {

    describe('(shared)', function () {

      beforeEach(function() {
        this.injectDependencies('$rootScope', 'factory');
      });

      it('can add annotation', function () {
        this.updateEntity(context.entity,
                          context.updateFuncName,
                          context.annotation,
                          context.addUrl,
                          _.pickBy(_.pick(context.annotation,
                                          [
                                            'annotationTypeId',
                                            'stringValue',
                                            'numberValue',
                                            'selectedValues'
                                          ]),
                                   _.identity),
                          context.response,
                          expectEntity,
                          failTest);
      });

      it('can remove an annotation', function () {
        var self = this;

        context.$httpBackend.whenDELETE(context.removeUrl).respond(201, { status: 'success', data: true });
        context.entity[context.removeFuncName](context.annotation)
          .then(self.expectParticipant)
          .catch(self.failTest);
        context.$httpBackend.flush();
      });

      it('fails when removing an invalid annotation', function () {
        var annotation = _.extend({}, context.annotation, { annotationTypeId: this.factory.stringNext() });
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

  return entityWithAnnotationsSharedSpec;
});
