/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @exports test.mixins.EntityTestSuiteMixin
 */
let EntityTestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * Also injects dependencies required by this mixin.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    const allDependencies = dependencies.concat([ '$httpBackend' ]);
    TestSuiteMixin.injectDependencies.call(this, ...allDependencies);
  },

  /**
   * Used to test entity creation.
   *
   * Attempts to create an entity with invalid properties are made, and the create function is expected
   * to throw an Error.
   *
   * @param {Array<object>} invalidProperties - the items in the array hold invalid values for the
   * property in the entity.
   *
   * @param {function} objCreateFun - the function to call to create the plain object, with the invalid
   * property, to pass to the entity's create function.
   *
   * @param {function(object)} entityCreateFun - the function that creates the entity from a plain object.
   */
  invalidPropertiesTest: function (invalidProperties, objCreateFun, entityCreateFun) {
    invalidProperties.forEach((invalidProperty) => {
      const obj = objCreateFun(invalidProperty);
      expect(() => entityCreateFun(obj)).toThrowError(/Invalid type/);
    });
  },

  /**
   * Used to test entity creation.
   *
   * Object are created with the missing properties and the create function is expected to throw an Error.
   *
   * @param {Array<object>} missingProperties - the names of the properties to omit.
   *
   * @param {function} objCreateFun - the function to call to create the plain object, with the missing
   * property, to pass to the entity's create function. This object is created for each element in
   * `missingProperties`
   *
   * @param {function(object)} entityCreateFun - the function that creates the entity from a plain object.
   *
   * @see #missingPropertiesTestAsync
   */
  missingPropertiesTest: function (missingProperties, objCreateFun, entityCreateFun) {
    missingProperties.forEach((missingProperty) => {
      const obj = objCreateFun(missingProperty);
      expect(() => entityCreateFun(obj)).toThrowError(/Missing required property/);
    });
  },

  /**
   * Used to test entity creation in an asynchronous fashion.
   *
   * Object are created with the missing properties and the create function is expected to throw an Error.
   *
   * @param {Array<object>} missingProperties - the names of the properties to omit.
   *
   * @param {function} objCreateFun - the function to call to create the plain object, with the missing
   * property, to pass to the entity's create function. This object is created for each element in
   * `missingProperties`
   *
   * @param {function(object)} entityCreateFun - the function that creates the entity from a plain object.
   *
   * @see #missingPropertiesTest
   */
  missingPropertiesTestAsync: function (missingProperties, objCreateFun, entityCreateFun) {
    missingProperties.forEach((missingProperty) => {
      const obj = objCreateFun(missingProperty)
      entityCreateFun(obj)
        .then(() => {
          fail('should not invoked')
        })
        .catch((err) => {
          expect(err.message).toContain('Missing required property')
        })
    });
  },

  /**
   * Used to test updating an attribute on an entity.
   *
   * @param {object} entity - An instance of the entity to be updated.
   *
   * @param {string} updateFuncName - The name of the update function on the entity to be tested.
   *
   * @param {(string[]|object)} updateParams - the parameters to pass to the update function. If this is
   * not an string array, it will be converted to a single item array.
   *
   * @param {string} url - the URL on the server where the request will be POSTed to.
   *
   * @param {string} reqJson - the JSON to pass along with the POST request.
   *
   * @param {object} reply - The mocked reply from the server.
   *
   * @param {function(object)} thenFunc the success function to be called if the update to the entity is
   *        successful.
   *
   * @param {function(string)} catchFunc the fail function to be called if the update to the entity fails.
   *
   * @param {boolean} [appendExpectedVersion=true] - If TRUE, the 'expectedVersion' property is added to
   *        the request JSON. Default value is TRUE.
   *
   * @return {undefined}
   *
   * @see #updateEntityWithCallback
   */
  updateEntity: function (entity,
                          updateFuncName,
                          updateParams,
                          url,
                          reqJson,
                          reply,
                          thenFunc,
                          catchFunc,
                          appendExpectedVersion = true) {
    expect(entity[updateFuncName]).toBeFunction();
    expect(thenFunc).toBeFunction();
    expect(catchFunc).toBeFunction();

    if (appendExpectedVersion) {
      Object.assign(reqJson, { expectedVersion: 0 });
    }
    this.$httpBackend.expectPOST(url, reqJson).respond(201, { status: 'success', data: reply });

    if (!Array.isArray(updateParams)) {
      updateParams = [ updateParams ];
    }

    entity[updateFuncName].apply(entity, updateParams).then(thenFunc).catch(catchFunc);
    this.$httpBackend.flush();
  },

  /**
   * Used to test updating an attribute on an entity.
   *
   * @param {function} func - The function to call that updates the entity.
   *
   * @param {string} url - the URL on the server where the request will be POSTed to.
   *
   * @param {string} reqJson - the JSON to pass along with the POST request.
   *
   * @param {object} reply - The mocked reply from the server.
   *
   * @return {undefined}
   *
   * @see #updateEntity
   */
  updateEntityWithCallback: function (func, url, reqJson, reply) {
    expect(url).not.toBeEmptyString();
    expect(reqJson).toBeObject();
    expect(reply).toBeObject();

    this.$httpBackend.expectPOST(url, reqJson).respond(201, { status: 'success', data: reply });
    func();
    this.$httpBackend.flush();
  },

  /**
   * Utility function that can be used in a promise `catch` clause.
   *
   * @param {object} error - the error stating why the promise was rejected.
   */
  failTest: function (error) {
    expect(error).toBeUndefined();
  }

}

EntityTestSuiteMixin = Object.assign({}, TestSuiteMixin, EntityTestSuiteMixin);

export { EntityTestSuiteMixin };
export default () => {};
