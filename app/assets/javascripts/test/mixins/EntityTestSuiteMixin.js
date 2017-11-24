/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @return {object} Object containing the functions that will be mixed in.
 */
/* @ngInject */
function EntityTestSuiteMixin($httpBackend, TestSuiteMixin) {

  return Object.assign(
    {
      updateEntity,
      invalidFieldsTest,
      missingFieldsTest,
      missingFieldsTestAsync,
      failTest
    },
    TestSuiteMixin);

  function invalidFieldsTest(invalidFields, objCreateFun, entityCreateFun) {
    invalidFields.forEach((invalidField) => {
      const obj = objCreateFun(invalidField);
      expect(() => entityCreateFun(obj)).toThrowError(/Invalid type/);
      });
  }

  function missingFieldsTest(missingFields, objCreateFun, entityCreateFun) {
    missingFields.forEach((missingField) => {
      const obj = objCreateFun(missingField);
      expect(() => entityCreateFun(obj)).toThrowError(/Missing required property/);
    });
  }

  function missingFieldsTestAsync(missingFields, objCreateFun, entityCreateFun) {
    missingFields.forEach((missingField) => {
      const obj = objCreateFun(missingField)
      entityCreateFun(obj)
        .then(() => {
          fail('should not invoked')
        })
        .catch((err) => {
          expect(err.message).toContain(':Missing required property:')
        })
    });
  }

  /**
   * Used to test updating an attribute on an entity.
   *
   * @param {Object} entity - An instance of the entity to be updated.
   *
   * @param {string} updateFuncName - The name of the update function on the entity to be tested.
   *
   * @param {(string[]|Object)} updateParams - the parameters to pass to the update function. If this is not
   * an string array, it will be converted to a single item array.
   *
   * @param {string} url - the URL on the server where the request will be POSTed to.
   *
   * @param {string} reqJson - the JSON to pass along with the POST request.
   *
   * @param {object} reply - The mocked reply from the server.
   *
   * @param {function(Object)} thenFunc the success function to be called if the update to the entity is
   *        successful.
   *
   * @param {function(string)} catchFunc the fail function to be called if the update to the entity fails.
   *
   * @param {boolean} [appendExpectedVersion] - If TRUE, the 'expectedVersion' field is added to the request
   *        JSON. Default value is TRUE.
   *
   * @return {void} nothing.
   */
  function updateEntity(entity,
                        updateFuncName,
                        updateParams,
                        url,
                        reqJson,
                        reply,
                        thenFunc,
                        catchFunc,
                        appendExpectedVersion) {
    expect(entity[updateFuncName]).toBeFunction();
    expect(thenFunc).toBeFunction();
    expect(catchFunc).toBeFunction();

    appendExpectedVersion = _.isBoolean(appendExpectedVersion) ? appendExpectedVersion : true;
    if (appendExpectedVersion) {
      Object.assign(reqJson, { expectedVersion: 0 });
    }
    $httpBackend.expectPOST(url, reqJson).respond(201, { status: 'success', data: reply });

    if (!Array.isArray(updateParams)) {
      updateParams = [ updateParams ];
    }

    entity[updateFuncName].apply(entity, updateParams).then(thenFunc).catch(catchFunc);
    $httpBackend.flush();
  }

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined();
  }

}

export default ngModule => ngModule.service('EntityTestSuiteMixin', EntityTestSuiteMixin)
