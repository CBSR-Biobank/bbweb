/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  function annotationTypeDataSharedSpec(context) {

    describe('(shared)', function() {

      var parentObj, annotationTypes, fakeEntities;

      beforeEach(function () {
        parentObj          = context.parentObj;
        annotationTypes    = context.annotationTypes;
        fakeEntities       = context.fakeEntities;
      });

      it('getAnnotationTypeDataById returns valid results', function() {
        _.each(annotationTypes, function (at) {
          expect(parentObj.getAnnotationTypeDataById(at.id).annotationType).toEqual(at);
        });

      });

      it('getAnnotationTypeDataById throws an error if not found', function() {
        var badId = fakeEntities.stringNext();
        expect(function () { parentObj.getAnnotationTypeDataById(badId); })
          .toThrow(new Error('annotation type data with id not found: ' + badId));
      });

      it('get as string returns valid results', function() {
        var expectedStrs = _.map(annotationTypes, function(at) {
          var atDataItem = _.findWhere(parentObj.annotationTypeData, { annotationTypeId: at.id });
          return at.name + ' ' + (atDataItem.required ? '(Req)' : '(N/R)');
        });

        expect(parentObj.getAnnotationTypeDataAsString()).toBe(expectedStrs.join(', '));
      });

    });
  }

  return annotationTypeDataSharedSpec;

});
